package cn.yxffcode.stupidmybatis.data;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Limitation {
  String offsetParam() default "";
  String limitParam();
}
