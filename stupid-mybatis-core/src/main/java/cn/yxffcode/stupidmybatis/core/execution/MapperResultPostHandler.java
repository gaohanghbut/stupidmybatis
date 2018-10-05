package cn.yxffcode.stupidmybatis.core.execution;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author gaohang
 */
public interface MapperResultPostHandler<A extends Annotation> {

  /**
   * 处理结果
   * @param annotation 处理的注解
   * @param type mapper接口的类型
   * @param method mapper接口上的方法
   * @param result mapper方法的结果
   */
  Object handle(A annotation, Class<?> type, Method method, Object proxy, Object result) throws Throwable;
}
