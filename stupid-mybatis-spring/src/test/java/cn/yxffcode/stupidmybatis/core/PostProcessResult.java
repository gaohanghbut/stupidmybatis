package cn.yxffcode.stupidmybatis.core;

import cn.yxffcode.stupidmybatis.core.execution.MapperResultHandler;
import cn.yxffcode.stupidmybatis.core.execution.MapperResultPostHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperResultHandler(PostProcessResult.PostProcesser.class)
public @interface PostProcessResult {

  /**
   * 处理返回结果的类
   */
  final class PostProcesser implements MapperResultPostHandler<PostProcessResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostProcessResult.class);

    @Override
    public Object handle(PostProcessResult postProcessResult, Class<?> type, Method method, Object proxy, Object result) throws Throwable {
      LOGGER.info("{}.{} result is {}", type.getName(), method.getName(), result);
      return result;
    }
  }
}
