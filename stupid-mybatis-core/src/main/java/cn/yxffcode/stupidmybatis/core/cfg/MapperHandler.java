package cn.yxffcode.stupidmybatis.core.cfg;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MapperHandler {
  Class<? extends MapperConfigHandler<?>> value();

  /**
   * 在mapper接口解析前还是解析后调用
   * @return
   */
  Order order() default Order.BEFORE_CONFIG_PARSE;

  enum Order {
    BEFORE_CONFIG_PARSE,
    AFTER_CONFIG_PARSE
  }
}
