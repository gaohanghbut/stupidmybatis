package cn.yxffcode.stupidmybatis.data;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Limitation {
  int offset() default 0;
  int limit();
}
