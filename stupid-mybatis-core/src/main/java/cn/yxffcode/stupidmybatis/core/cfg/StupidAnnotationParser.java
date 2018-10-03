package cn.yxffcode.stupidmybatis.core.cfg;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.session.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author gaohang
 */
public class StupidAnnotationParser extends MapperAnnotationBuilder {
  private final Class<?> type;
  private MapperBuilderAssistant mapperBuilderAssistant;

  private Map<Class<? extends MapperConfigHandler>, MapperConfigHandler> mapperHandlers = Maps.newHashMap();

  public StupidAnnotationParser(Configuration configuration, Class<?> type) {
    super(configuration, type);
    this.type = type;
    this.mapperBuilderAssistant = (MapperBuilderAssistant) Reflections.getField("assistant", this);
    if (this.mapperBuilderAssistant.getCurrentNamespace() == null) {
      this.mapperBuilderAssistant.setCurrentNamespace(type.getName());
    }
  }

  @Override
  public void parse() {
    Method[] methods = type.getMethods();
    invokeMapperHandlers(methods, MapperConfHandler.Order.BEFORE_CONFIG_PARSE);
    super.parse();
    invokeMapperHandlers(methods, MapperConfHandler.Order.AFTER_CONFIG_PARSE);
  }

  private void invokeMapperHandlers(Method[] methods, MapperConfHandler.Order order) {
    for (Method method : methods) {
      Annotation[] annotations = method.getAnnotations();
      if (annotations == null || annotations.length == 0) {
        continue;
      }
      parseAnnotations(order, method, annotations);
    }
    parseAnnotations(order, null, type.getAnnotations());
  }

  private void parseAnnotations(MapperConfHandler.Order order, Method method, Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      MapperConfHandler mapperConfHandler = annotation.annotationType().getAnnotation(MapperConfHandler.class);
      if (mapperConfHandler == null || mapperConfHandler.order() != order) {
        continue;
      }
      parseAnnotation(method, mapperConfHandler, annotation);
    }
  }

  private void parseAnnotation(Method method, MapperConfHandler mapperConfHandler, Annotation annotation) {
    Class<? extends MapperConfigHandler<?>> handlerType = mapperConfHandler.value();
    //创建实例
    MapperConfigHandler mapperConfigHandler = mapperHandlers.get(handlerType);
    if (mapperConfigHandler == null) {
      try {
        mapperConfigHandler = handlerType.newInstance();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
      mapperHandlers.put(handlerType, mapperConfigHandler);
    }
    try {
      mapperConfigHandler.handleAnnotation(annotation, type, method, mapperBuilderAssistant);
    } catch (Throwable throwable) {
      throw Throwables.propagate(throwable);
    }
  }

}
