package cn.yxffcode.stupidmybatis.core.cfg;

import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author gaohang
 */
public interface MapperConfigHandler<T extends Annotation> {
  void handleAnnotation(T annotation, Class<?> type, Method method, MapperBuilderAssistant assistant) throws Throwable;
}
