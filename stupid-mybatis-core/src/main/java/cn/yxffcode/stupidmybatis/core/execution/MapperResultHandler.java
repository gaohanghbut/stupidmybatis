package cn.yxffcode.stupidmybatis.core.execution;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MapperResultHandler {
  Class<? extends MapperResultPostHandler> value();
}
