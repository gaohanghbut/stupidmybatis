package cn.yxffcode.stupidmybatis.data.sql;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MacroDeclare {
  Macro[] value() default {};
}
