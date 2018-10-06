package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.data.cfg.SpecifiedDeleteProvider;
import cn.yxffcode.stupidmybatis.data.cfg.SpecifiedUpdateProvider;
import cn.yxffcode.stupidmybatis.data.parser.MapperAnnotationBuilder;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Map;

import static cn.yxffcode.stupidmybatis.data.utils.OrmUtils.getOrmConfig;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(ORMDelete.Config.class)
public @interface ORMDelete {
  /**
   * @return 更新条件，DO的属性，默认为ID
   */
  String[] conditions() default {};

  /**
   * @return 更新是否忽略空属性
   */
  boolean ignoreNull() default true;

  final class Config implements MapperConfigHandler<ORMDelete> {

    @Override
    public void handleAnnotation(ORMDelete annotation, Class<?> mapperInterface, Method method, MapperBuilderAssistant assistant) throws Throwable {
      //注册MappedStatement
      SpecifiedDeleteProvider deleteProvider = new SpecifiedDeleteProvider(ORMDelete.SqlProvider.class, "getSql");
      MapperAnnotationBuilder.parseStatement(assistant, mapperInterface, method, deleteProvider, null);
    }
  }

  final class SqlProvider {

    public String getSql(Object params, ProviderContext providerContext) {

      Class<?> ormEntityClass = OrmUtils.getOrmEntityClass(providerContext.getMapperType());
      if (ormEntityClass == null) {
        throw new StupidMybatisOrmException("orm config missed, cannot find result type for " + providerContext.getMapperType().getName());
      }

      TableMetaCache.ORMConfig ormConfig = getOrmConfig(ormEntityClass);

      SQL sql = new SQL().DELETE_FROM(ormConfig.getOrm().tableName());

      //append condition
      String[] conditions = OrmUtils.getConditions(providerContext.getMapperMethod(), ormConfig, ORMDelete.class);
      for (String property : conditions) {
        String column = ormConfig.getColumn(property);
        if (column == null) {
          throw new StupidMybatisOrmException("no orm mapping config for property:" + property);
        }
        sql.WHERE(column + " = #{" + property + '}').AND();
      }
      sql.WHERE("1 = 1");
      return sql.toString();
    }
  }
}
