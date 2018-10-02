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
}
