package cn.yxffcode.stupidmybatis.core;

import com.google.common.reflect.AbstractInvocationHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 16/8/5.
 */
public class DaoProxy extends AbstractInvocationHandler {

  private final Object target;

  public DaoProxy(final Object target) {
    this.target = checkNotNull(target);
  }

  public static Object wrapNotNull(Object target) {
    checkNotNull(target);
    return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        target.getClass().getInterfaces(), new DaoProxy(target));
  }

  @Override
  protected Object handleInvocation(final Object proxy,
                                    final Method method,
                                    final Object[] args) throws Throwable {
    final Paged paged = method.getAnnotation(Paged.class);
    if (paged == null) {
      return method.invoke(target, args);
    }
    DaoQueryPageContextHolder.set(paged);
    try {
      return method.invoke(target, args);
    } finally {
      DaoQueryPageContextHolder.remove();
    }
  }
}
