package cn.yxffcode.stupidmybatis.core.statement;

import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MybatisConfigParser;
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
@MapperConfHandler(TypeResultMap.TypeResultMapHandler.class)
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
      MybatisConfigParser.parseResultsAndConstructorArgs(type, method, assistant);
    }
  }

}
