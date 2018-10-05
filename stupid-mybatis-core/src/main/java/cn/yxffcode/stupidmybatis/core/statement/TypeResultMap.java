package cn.yxffcode.stupidmybatis.core.statement;

import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MybatisConfigUtils;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author gaohang
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(value = TypeResultMap.TypeResultMapHandler.class, order = MapperConfHandler.Order.BEFORE_CONFIG_PARSE)
public @interface TypeResultMap {

  /**
   * 结果类型
   */
  Class<?> resultType() default Object.class;

  /**
   * @return resultMap id
   */
  String id() default "";

  /**
   * @return 结果映射
   */
  Result[] value() default {};

  final class TypeResultMapHandler implements MapperConfigHandler<TypeResultMap> {
    @Override
    public void handleAnnotation(TypeResultMap annotation, Class<?> type, Method method, MapperBuilderAssistant assistant) {
      MybatisConfigUtils.parseResultsAndConstructorArgs(type, method, assistant);
    }
  }

}
