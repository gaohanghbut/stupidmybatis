package cn.yxffcode.stupidmybatis.core.execution;

import java.lang.annotation.*;

/**
 * 结果处理接口
 *
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MapperResultHandler {
  Class<? extends MapperResultPostHandler> value();
}
