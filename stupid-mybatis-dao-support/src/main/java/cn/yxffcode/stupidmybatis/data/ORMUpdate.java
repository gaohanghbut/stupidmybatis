package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
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
@MapperConfHandler(ORMUpdate.Config.class)
public @interface ORMUpdate {
  /**
   * @return 需要查询的DO属性(不是表的字段)，默认为@ORM中的resultMap上所有的属性
   */
  String[] properties() default {};

  /**
   * @return 更新条件，DO的属性,默认为ID
   */
  String[] conditions() default {};

  /**
   * @return 更新是否忽略空属性
   */
  boolean ignoreNull() default true;

  final class Config implements MapperConfigHandler<ORMUpdate> {

    @Override
    public void handleAnnotation(ORMUpdate annotation, Class<?> mapperInterface, Method method, MapperBuilderAssistant assistant) throws Throwable {
      //注册MappedStatement
      SpecifiedUpdateProvider updateProvider = new SpecifiedUpdateProvider(ORMUpdate.SqlProvider.class, "getSql");
      MapperAnnotationBuilder.parseStatement(assistant, mapperInterface, method, updateProvider, null);
    }
  }

  final class SqlProvider {

    public String getSql(Object params, ProviderContext providerContext) {

      Class<?> ormEntityClass = OrmUtils.getOrmEntityClass(providerContext.getMapperType());
      if (ormEntityClass == null) {
        throw new StupidMybatisOrmException("orm config missed, cannot find result type for " + providerContext.getMapperType().getName());
      }

      TableMetaCache.ORMConfig ormConfig = getOrmConfig(ormEntityClass);

      String[] properties = OrmUtils.getProperties(providerContext.getMapperMethod(), ormConfig, ORMUpdate.class);
      boolean ignoreNull = providerContext.getMapperMethod().getDeclaredAnnotation(ORMUpdate.class).ignoreNull();

      SQL sql = new SQL().UPDATE(ormConfig.getOrm().tableName());
      for (String property : properties) {
        String column = ormConfig.getColumn(property);
        if (column == null) {
          throw new StupidMybatisOrmException("no orm mapping config for property:" + property);
        }

        if (ignoreNull) {
          Object value = getFieldValue(params, property, ormEntityClass);
          if (value == null) {
            continue;
          }
        }
        sql.SET(column + " = #{" + property + '}');
      }
      //append condition
      String[] conditions = OrmUtils.getConditions(providerContext.getMapperMethod(), ormConfig, ORMUpdate.class);
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

    private Object getFieldValue(Object params, String property, Class<?> ormEntityClass) {
      if (ormEntityClass.isAssignableFrom(params.getClass())) {
        return Reflections.getField(property, params);
      } else if (params instanceof Map) {
        return ((Map) params).get(property);
      }
      throw new IllegalArgumentException("plz add @Param(property) for parameters");
    }

  }
}
