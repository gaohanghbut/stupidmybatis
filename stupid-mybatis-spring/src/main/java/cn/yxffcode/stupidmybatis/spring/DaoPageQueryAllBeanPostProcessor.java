package cn.yxffcode.stupidmybatis.spring;

import cn.yxffcode.stupidmybatis.core.DaoProxy;
import cn.yxffcode.stupidmybatis.core.Paged;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 配置在spring中,用于实现DAO的代理
 *
 * @author gaohang on 16/8/4.
 */
public class DaoPageQueryAllBeanPostProcessor implements BeanPostProcessor {
  private final Class<? extends Annotation> annotation;

  public DaoPageQueryAllBeanPostProcessor(final String annotationName) {
    try {
      this.annotation = (Class<? extends Annotation>) Class.forName(annotationName);
      if (!annotation.isAnnotation()) {
        throw new IllegalArgumentException(annotationName + " is not an annotation");
      }
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Object postProcessBeforeInitialization(final Object bean, final String beanName)
      throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean, final String beanName)
      throws BeansException {
    Class<?> type = bean.getClass();
    while (type != Object.class) {
      final Annotation annotation = type.getAnnotation(this.annotation);
      if (annotation != null && isMarkedByPaged(type)) {
        return DaoProxy.wrapNotNull(bean);
      }
      type = type.getSuperclass();
    }
    final Class<?>[] interfaces = bean.getClass().getInterfaces();
    for (Class<?> in : interfaces) {
      final Annotation annotation = in.getAnnotation(this.annotation);
      if (annotation != null && isMarkedByPaged(in)) {
        return DaoProxy.wrapNotNull(bean);
      }
    }
    return bean;
  }

  private boolean isMarkedByPaged(Class<?> type) {
    final Method[] methods = type.getMethods();
    for (Method method : methods) {
      if (method.getAnnotation(Paged.class) != null) {
        return true;
      }
    }
    return false;
  }
}
