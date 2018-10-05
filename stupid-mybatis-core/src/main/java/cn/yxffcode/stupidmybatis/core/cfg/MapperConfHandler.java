package cn.yxffcode.stupidmybatis.core.cfg;

import java.lang.annotation.*;

/**
 * 配置解析接口
 *
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MapperConfHandler {
  Class<? extends MapperConfigHandler<?>> value();

  /**
   * 在mapper接口解析前还是解析后调用
   *
   * @return
   */
  Order order() default Order.AFTER_CONFIG_PARSE;

  enum Order {
    BEFORE_CONFIG_PARSE,
    AFTER_CONFIG_PARSE
  }
}
