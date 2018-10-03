package cn.yxffcode.stupidmybatis.core;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(value = Limit.Config.class, order = MapperConfHandler.Order.AFTER_CONFIG_PARSE)
public @interface Limit {

  /**
   * @return limit 的大小
   */
  int value();

  /**
   * 处理返回结果的类
   */
  final class Config implements MapperConfigHandler<Limit> {
    @Override
    public void handleAnnotation(Limit limit, Class<?> type, Method method, MapperBuilderAssistant assistant) throws Throwable {
      //通过assistant注册配置，不清楚可看看mybatis源码
      String statementId = type.getName() + '.' + method.getName();
      MappedStatement mappedStatement = assistant.getConfiguration().getMappedStatement(statementId);

      Reflections.setField(mappedStatement, "sqlSource", new SqlSource() {
        private final SqlSource delegate = mappedStatement.getSqlSource();
        private final Configuration configuration = assistant.getConfiguration();

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
          BoundSql boundSql = delegate.getBoundSql(parameterObject);
          return new BoundSql(configuration, boundSql.getSql() + " limit " + limit.value(), boundSql.getParameterMappings(), boundSql.getParameterObject());
        }
      });
    }

  }
}