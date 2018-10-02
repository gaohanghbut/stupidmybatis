package cn.yxffcode.stupidmybatis.core.statement;

import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MybatisConfigParser;
import cn.yxffcode.stupidmybatis.core.execution.MapperResultHandler;
import cn.yxffcode.stupidmybatis.core.execution.MapperResultPostHandler;
import com.google.common.collect.Lists;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.lang.annotation.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperHandler(value = MapperMethod.MapperMethodHandler.class, order = MapperHandler.Order.AFTER_CONFIG_PARSE)
@MapperResultHandler(MapperMethod.MapperMethodHandler.class)
public @interface MapperMethod {

  /**
   * @return 结果映射
   */
  String value();

  final class MapperMethodHandler implements MapperConfigHandler<MapperMethod>, MapperResultPostHandler {

    @Override
    public void handleAnnotation(MapperMethod annotation, Class<?> type, Method method, MapperBuilderAssistant assistant) throws Throwable {
      //将ResultMap设置成返回Map
      MybatisConfigParser.registMapResult(type, method, assistant);
    }

    @Override
    public Object handle(Class<?> type, Method mtd, Object proxy, Object result) throws Throwable {
      MapperMethod mapperMethod = mtd.getAnnotation(MapperMethod.class);
      if (mapperMethod == null) {
        return result;
      }
      Method methodToMapper = getResultMapperMethod(type, mapperMethod);
      if (methodToMapper == null || !methodToMapper.isDefault()) {
        throw new IllegalAccessException("cannot call method without object");
      }
      //方法调用
      Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
      constructor.setAccessible(true);
      Class<?> declaringClass = methodToMapper.getDeclaringClass();
      int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;
      MethodHandle methodHandle = constructor.newInstance(declaringClass, allModes).unreflectSpecial(methodToMapper, declaringClass).bindTo(proxy);

      if (result instanceof Iterable) {
        List<Object> results = Lists.newArrayList();
        Iterable iterable = (Iterable) result;
        for (Object r : iterable) {
          Object obj = methodHandle.invokeWithArguments(result);
          if (obj != null) {
            results.add(obj);
          }
        }
        return results;
      }
      return methodHandle.invokeWithArguments(result);
    }

    private Method getResultMapperMethod(Class<?> type, MapperMethod mapperMethod) {
      Method methodToMapper = null;
      Method[] methods = type.getMethods();
      for (Method method : methods) {
        if (method.getName().equals(mapperMethod.value())) {
          methodToMapper = method;
        }
      }
      return methodToMapper;
    }
  }

}