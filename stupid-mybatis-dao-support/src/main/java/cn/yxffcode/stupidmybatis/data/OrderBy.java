package cn.yxffcode.stupidmybatis.data;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OrderBy {
  Order[] value() default {};

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @interface Order {

    String value();

    Sort sort() default Sort.ASC;
  }

  enum Sort {
    ASC, DESC
  }
}
