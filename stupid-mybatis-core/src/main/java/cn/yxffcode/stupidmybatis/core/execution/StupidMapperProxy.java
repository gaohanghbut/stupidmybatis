package cn.yxffcode.stupidmybatis.core.execution;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentMap;

/**
 * @author gaohang
 */
public class StupidMapperProxy implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private SqlSession sqlSession;
  private ConcurrentMap<Class<?>, MapperResultPostHandler> mapperResultPostHandlers = Maps.newConcurrentMap();

  private <T> StupidMapperProxy(SqlSession sqlSession) {
    this.sqlSession = sqlSession;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }

    if (method.isDefault()) {
      return invokeDefaultMethod(proxy, method, args);
    }

    final Class<?> declaringInterface = findDeclaringInterface(proxy, method);
    final MapperMethod mapperMethod = new MapperMethod(declaringInterface, method, sqlSession);
    Object result = mapperMethod.execute(args);
    if (result == null && method.getReturnType().isPrimitive() && !method.getReturnType().equals(Void.TYPE)) {
      throw new BindingException("Mapper method '" + method.getName() + "' (" + method.getDeclaringClass() + ") attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    //结果的后置处理
    Annotation[] annotations = method.getAnnotations();
    if (annotations == null || annotations.length == 0) {
      return result;
    }

    //先处理@MapperMethod
    cn.yxffcode.stupidmybatis.core.statement.MapperMethod annotation = method.getAnnotation(cn.yxffcode.stupidmybatis.core.statement.MapperMethod.class);
    if (annotation != null) {
      result = handlePostProcessAnnotation(method, declaringInterface, proxy, result, annotation);
    }

    result = postProcessResult(method, declaringInterface, proxy, result, annotations);

    return result;
  }

  private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
    Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
    constructor.setAccessible(true);
    Class<?> declaringClass = method.getDeclaringClass();
    int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;
    MethodHandle methodHandle = constructor.newInstance(declaringClass, allModes).unreflectSpecial(method, declaringClass).bindTo(proxy);
    return methodHandle.invokeWithArguments(args);
  }

  private Object postProcessResult(Method method, Class<?> declaringInterface, Object proxy, Object result, Annotation[] annotations) throws InstantiationException, IllegalAccessException {
    for (Annotation annotation : annotations) {
      if (annotation instanceof cn.yxffcode.stupidmybatis.core.statement.MapperMethod) {
        continue;
      }
      result = handlePostProcessAnnotation(method, declaringInterface, proxy, result, annotation);
    }
    return result;
  }

  private Object handlePostProcessAnnotation(Method method, Class<?> declaringInterface, Object proxy, Object result, Annotation annotation) throws InstantiationException, IllegalAccessException {
    MapperResultHandler mapperResultHandler = annotation.annotationType().getAnnotation(MapperResultHandler.class);
    if (mapperResultHandler == null) {
      return result;
    }
    MapperResultPostHandler mapperResultPostHandler = getMapperResultPostHandler(mapperResultHandler);
    try {
      result = mapperResultPostHandler.handle(declaringInterface, method, proxy, result);
    } catch (Throwable throwable) {
      throw Throwables.propagate(throwable);
    }
    return result;
  }

  private MapperResultPostHandler getMapperResultPostHandler(MapperResultHandler mapperResultHandler) throws InstantiationException, IllegalAccessException {
    Class<? extends MapperResultPostHandler> handlerType = mapperResultHandler.value();
    MapperResultPostHandler mapperResultPostHandler = mapperResultPostHandlers.get(handlerType);
    if (mapperResultPostHandler == null) {
      mapperResultPostHandler = handlerType.newInstance();
      MapperResultPostHandler exists = mapperResultPostHandlers.putIfAbsent(handlerType, mapperResultPostHandler);
      if (exists != null) {
        mapperResultPostHandler = exists;
      }
    }
    return mapperResultPostHandler;
  }

  private Class<?> findDeclaringInterface(Object proxy, Method method) {
    Class<?> declaringInterface = null;
    for (Class<?> iface : proxy.getClass().getInterfaces()) {
      try {
        Method m = iface.getMethod(method.getName(), method.getParameterTypes());
        if (declaringInterface != null) {
          throw new BindingException("Ambiguous method mapping.  Two mapper interfaces contain the identical method signature for " + method);
        } else if (m != null) {
          declaringInterface = iface;
        }
      } catch (Exception e) {
      }
    }
    if (declaringInterface == null) {
      throw new BindingException("Could not find interface with the given method " + method);
    }
    return declaringInterface;
  }

  @SuppressWarnings("unchecked")
  public static <T> T newMapperProxy(Class<T> mapperInterface, SqlSession sqlSession) {
    ClassLoader classLoader = mapperInterface.getClassLoader();
    Class<?>[] interfaces = new Class[]{mapperInterface};
    return (T) Proxy.newProxyInstance(classLoader, interfaces, new StupidMapperProxy(sqlSession));
  }

}
