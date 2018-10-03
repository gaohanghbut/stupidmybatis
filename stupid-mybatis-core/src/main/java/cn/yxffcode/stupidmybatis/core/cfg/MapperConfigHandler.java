package cn.yxffcode.stupidmybatis.core.cfg;

import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 处理注解配置的接口定义
 * @author gaohang
 */
public interface MapperConfigHandler<T extends Annotation> {
  /**
   * 处理注解配置
   * @param annotation 配置的注解
   * @param type mapper接口
   * @param method mapper接口上标了此注解的方法
   * @param assistant 配置的builder工具
   */
  void handleAnnotation(T annotation, Class<?> type, Method method, MapperBuilderAssistant assistant) throws Throwable;
}
