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
    invokeMapperHandlers(methods, MapperHandler.Order.BEFORE_CONFIG_PARSE);
    super.parse();
    invokeMapperHandlers(methods, MapperHandler.Order.AFTER_CONFIG_PARSE);
  }

  private void invokeMapperHandlers(Method[] methods, MapperHandler.Order order) {
    for (Method method : methods) {
      Annotation[] annotations = method.getAnnotations();
      if (annotations == null || annotations.length == 0) {
        continue;
      }
      for (Annotation annotation : annotations) {
        MapperHandler mapperHandler = annotation.annotationType().getAnnotation(MapperHandler.class);
        if (mapperHandler == null || mapperHandler.order() != order) {
          continue;
        }
        parseAnnotation(method, mapperHandler, annotation);
      }
    }
  }

  private void parseAnnotation(Method method, MapperHandler mapperHandler, Annotation annotation) {
    Class<? extends MapperConfigHandler<?>> handlerType = mapperHandler.value();
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
