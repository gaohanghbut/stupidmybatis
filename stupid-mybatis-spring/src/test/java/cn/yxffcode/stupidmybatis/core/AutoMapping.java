package cn.yxffcode.stupidmybatis.core;

import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(AutoMapping.Config.class)
public @interface AutoMapping {

  /**
   * 处理返回结果的类
   */
  final class Config implements MapperConfigHandler<AutoMapping> {
    @Override
    public void handleAnnotation(AutoMapping annotation, Class<?> type, Method method, MapperBuilderAssistant assistant) throws Throwable {
      //通过assistant注册配置，不清楚可看看mybatis源码
    }
  }
}