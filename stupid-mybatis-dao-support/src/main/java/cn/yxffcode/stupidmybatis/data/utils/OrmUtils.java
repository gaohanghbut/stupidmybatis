package cn.yxffcode.stupidmybatis.data.utils;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.data.BaseDataAccess;
import cn.yxffcode.stupidmybatis.data.StupidMybatisOrmException;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author gaohang
 */
public abstract class OrmUtils {
  private OrmUtils() {
  }

  public static Class<?> getOrmEntityClass(Class<?> mapperInterface) {
    Type[] genericInterfaces = mapperInterface.getGenericInterfaces();

    Class<?> beanType = null;
    for (Type genericInterface : genericInterfaces) {
      if (genericInterface instanceof ParameterizedType) {

        //取出类型参数
        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
        if (parameterizedType.getRawType() != BaseDataAccess.class) {
          continue;
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Type actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
          beanType = (Class<?>) actualTypeArgument;
          break;
        }
      }

    }
    return beanType;
  }

  public static TableMetaCache.ORMConfig getOrmConfig(Class<?> beanType) {
    TableMetaCache.ORMConfig ormConfig = TableMetaCache.getInstance().getORMConfig(beanType);
    if (ormConfig == null) {
      throw new StupidMybatisOrmException("no orm config found for " + beanType);
    }
    return ormConfig;
  }

  public static String[] getProperties(Method method, TableMetaCache.ORMConfig ormConfig, Class<? extends Annotation> ormAnnotation) {
    Annotation annotation = method.getAnnotation(ormAnnotation);
    String[] selectProperties = Reflections.call(annotation, "properties");
    if (selectProperties == null || selectProperties.length == 0) {
      Set<String> propertySet = ormConfig.getProperties();
      if (propertySet == null || propertySet.isEmpty()) {
        throw new StupidMybatisOrmException("cannot find orm mapping config for mapperInterface:" + method.getDeclaringClass() + '.' + method.getName());
      }
      selectProperties = new String[propertySet.size()];
      propertySet.toArray(selectProperties);
    }
    return selectProperties;
  }

  public static String[] getConditions(Method method, TableMetaCache.ORMConfig ormConfig, Class<? extends Annotation> ormAnnotation) {
    Annotation annotation = method.getAnnotation(ormAnnotation);
    String[] conditions = Reflections.call(annotation, "conditions");
    if (conditions == null || conditions.length == 0) {
      conditions = ormConfig.getOrm().primaryKey().keyColumns();
    }
    return conditions;
  }
}
