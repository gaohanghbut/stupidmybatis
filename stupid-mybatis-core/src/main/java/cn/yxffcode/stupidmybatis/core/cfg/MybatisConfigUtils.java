package cn.yxffcode.stupidmybatis.core.cfg;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.statement.TypeResultMap;
import com.google.common.base.Strings;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.reflect.*;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author gaohang
 */
public abstract class MybatisConfigUtils {

  private MybatisConfigUtils() {
  }

  public static void parseResultsAndConstructorArgs(Class<?> type, Method method, MapperBuilderAssistant assistant) {
    TypeResultMap results = method == null ? type.getAnnotation(TypeResultMap.class) : method.getAnnotation(TypeResultMap.class);
    if (results == null) {
      return;
    }
    if (method == null) {
      checkArgument(!Strings.isNullOrEmpty(results.id()));
      checkArgument(results.resultType() != Object.class);
    }
    Class<?> returnType = method == null ? results.resultType() : getReturnType(method);
    if (returnType != null) {
      ConstructorArgs args = method == null ? null : method.getAnnotation(ConstructorArgs.class);
      TypeDiscriminator typeDiscriminator = method == null ? null : method.getAnnotation(TypeDiscriminator.class);
      String resultMapId = Strings.isNullOrEmpty(results.id()) ? method.getName() : results.id();
      applyResultMap(resultMapId, returnType, type, assistant, argsIf(args), resultsIf(results), typeDiscriminator);
    }
  }

  /**
   * 添加Map类型的ResultMap，覆盖已有的ResultMap
   *
   * @param type
   * @param method
   * @param assistant
   */
  public static void registMapResult(Class<?> type, Method method, MapperBuilderAssistant assistant) {
    String resultMapId = generateResultMapName(type, method);
    Configuration configuration = assistant.getConfiguration();
    if (!configuration.hasResultMap(resultMapId)) {
      applyResultMap(resultMapId, Map.class, type, assistant, argsIf(null), resultsIf(null), null);
    } else {
      ResultMap resultMap = configuration.getResultMap(resultMapId);
      Reflections.setField(resultMap, "type", Map.class);
    }
  }

  private static Result[] resultsIf(TypeResultMap results) {
    return results == null ? new Result[0] : results.value();
  }

  private static Arg[] argsIf(ConstructorArgs args) {
    return args == null ? new Arg[0] : args.value();
  }

  private static Class<?> getReturnType(Method method) {
    Class<?> returnType = method.getReturnType();
    if (Collection.class.isAssignableFrom(returnType)) {
      Type returnTypeParameter = method.getGenericReturnType();
      if (returnTypeParameter instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
        if (actualTypeArguments != null && actualTypeArguments.length == 1) {
          returnTypeParameter = actualTypeArguments[0];
          if (returnTypeParameter instanceof Class) {
            returnType = (Class<?>) returnTypeParameter;
          } else if (returnTypeParameter instanceof ParameterizedType) { // (issue #443) actual type can be a also a parametrized type
            returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
          } else if (returnTypeParameter instanceof GenericArrayType) {
            Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
            returnType = Array.newInstance(componentType, 0).getClass(); // (issue #525) support List<byte[]>
          }
        }
      }
    } else if (method.isAnnotationPresent(MapKey.class) && Map.class.isAssignableFrom(returnType)) {
      // (issue 504) Do not look into Maps if there is not MapKey annotation
      Type returnTypeParameter = method.getGenericReturnType();
      if (returnTypeParameter instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) returnTypeParameter).getActualTypeArguments();
        if (actualTypeArguments != null && actualTypeArguments.length == 2) {
          returnTypeParameter = actualTypeArguments[1];
          if (returnTypeParameter instanceof Class) {
            returnType = (Class<?>) returnTypeParameter;
          } else if (returnTypeParameter instanceof ParameterizedType) { // (issue 443) actual type can be a also a parametrized type
            returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
          }
        }
      }
    }

    return returnType;
  }

  private static String generateResultMapName(Class<?> type, Method method) {
    StringBuilder suffix = new StringBuilder();
    for (Class<?> c : method.getParameterTypes()) {
      suffix.append("-");
      suffix.append(c.getSimpleName());
    }
    if (suffix.length() < 1) {
      suffix.append("-void");
    }
    return type.getName() + "." + method.getName() + suffix;
  }

  private static void applyResultMap(String resultMapId, Class<?> returnType, Class<?> type, MapperBuilderAssistant assistant, Arg[] args, Result[] results, TypeDiscriminator discriminator) {
    List<ResultMapping> resultMappings = new ArrayList<>();
    applyConstructorArgs(args, returnType, resultMappings, assistant);
    applyResults(results, returnType, resultMappings, type, assistant);
    Discriminator disc = applyDiscriminator(resultMapId, returnType, discriminator, assistant);
    assistant.addResultMap(resultMapId, returnType, null, disc, resultMappings, null); // TODO add AutoMappingBehaviour
    createDiscriminatorResultMaps(resultMapId, returnType, type, assistant, discriminator);
  }

  private static void createDiscriminatorResultMaps(String resultMapId, Class<?> resultType, Class<?> type, MapperBuilderAssistant assistant, TypeDiscriminator discriminator) {
    if (discriminator != null) {
      for (Case c : discriminator.cases()) {
        String caseResultMapId = resultMapId + "-" + c.value();
        List<ResultMapping> resultMappings = new ArrayList<>();
        applyConstructorArgs(c.constructArgs(), resultType, resultMappings, assistant); // issue #136
        applyResults(c.results(), resultType, resultMappings, type, assistant);
        assistant.addResultMap(caseResultMapId, c.type(), resultMapId, null, resultMappings, null); // TODO add AutoMappingBehaviour
      }
    }
  }

  private static void applyResults(Result[] results, Class<?> resultType, List<ResultMapping> resultMappings, Class<?> type, MapperBuilderAssistant assistant) {
    for (Result result : results) {
      ArrayList<ResultFlag> flags = new ArrayList<>();
      if (result.id()) flags.add(ResultFlag.ID);
      Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) result.typeHandler();
      ResultMapping resultMapping = assistant.buildResultMapping(
          resultType,
          nullOrEmpty(result.property()),
          nullOrEmpty(result.column()),
          result.javaType() == void.class ? null : result.javaType(),
          result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
          hasNestedSelect(result) ? nestedSelectId(result, type) : null,
          null,
          null,
          null,
          typeHandlerClass == UnknownTypeHandler.class ? null : typeHandlerClass,
          flags);
      resultMappings.add(resultMapping);
    }
  }

  private static String nestedSelectId(Result result, Class<?> type) {
    String nestedSelect = result.one().select();
    if (nestedSelect.length() < 1) {
      nestedSelect = result.many().select();
    }
    if (!nestedSelect.contains(".")) {
      nestedSelect = type.getName() + "." + nestedSelect;
    }
    return nestedSelect;
  }

  private static boolean hasNestedSelect(Result result) {
    return result.one().select().length() > 0 || result.many().select().length() > 0;
  }

  private static void applyConstructorArgs(Arg[] args, Class<?> resultType, List<ResultMapping> resultMappings, MapperBuilderAssistant assistant) {
    for (Arg arg : args) {
      ArrayList<ResultFlag> flags = new ArrayList<>();
      flags.add(ResultFlag.CONSTRUCTOR);
      if (arg.id()) flags.add(ResultFlag.ID);
      Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) arg.typeHandler();
      ResultMapping resultMapping = assistant.buildResultMapping(
          resultType,
          null,
          nullOrEmpty(arg.column()),
          arg.javaType() == void.class ? null : arg.javaType(),
          arg.jdbcType() == JdbcType.UNDEFINED ? null : arg.jdbcType(),
          nullOrEmpty(arg.select()),
          nullOrEmpty(arg.resultMap()),
          null,
          null,
          typeHandlerClass == UnknownTypeHandler.class ? null : typeHandlerClass,
          flags);
      resultMappings.add(resultMapping);
    }
  }

  private static String nullOrEmpty(String value) {
    return value == null || value.trim().length() == 0 ? null : value;
  }

  private static Discriminator applyDiscriminator(String resultMapId, Class<?> resultType, TypeDiscriminator discriminator, MapperBuilderAssistant assistant) {
    if (discriminator != null) {
      String column = discriminator.column();
      Class<?> javaType = discriminator.javaType() == void.class ? String.class : discriminator.javaType();
      JdbcType jdbcType = discriminator.jdbcType() == JdbcType.UNDEFINED ? null : discriminator.jdbcType();
      Class<? extends TypeHandler<?>> typeHandler = discriminator.typeHandler() == UnknownTypeHandler.class ? null
          : (Class<? extends TypeHandler<?>>) discriminator.typeHandler();
      Case[] cases = discriminator.cases();
      Map<String, String> discriminatorMap = new HashMap<>();
      for (Case c : cases) {
        String value = c.value();
        String caseResultMapId = resultMapId + "-" + value;
        discriminatorMap.put(value, caseResultMapId);
      }
      return assistant.buildDiscriminator(resultType, column, javaType, jdbcType, typeHandler, discriminatorMap);
    }
    return null;
  }

}
