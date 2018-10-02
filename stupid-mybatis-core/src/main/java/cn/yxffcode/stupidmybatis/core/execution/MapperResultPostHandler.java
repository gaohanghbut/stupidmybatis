package cn.yxffcode.stupidmybatis.core.execution;

import java.lang.reflect.Method;

/**
 * @author gaohang
 */
public interface MapperResultPostHandler {

  /**
   * 处理结果
   */
  Object handle(Class<?> type, Method method, Object proxy, Object result) throws Throwable;
}
