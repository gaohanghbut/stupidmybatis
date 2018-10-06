package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.data.cfg.SpecifiedResultMap;
import cn.yxffcode.stupidmybatis.data.cfg.SpecifiedSelectProvider;
import cn.yxffcode.stupidmybatis.data.parser.MapperAnnotationBuilder;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Map;

import static cn.yxffcode.stupidmybatis.data.utils.OrmUtils.getOrmConfig;

/**
 * 类似于@Select/@SelectProvider，提供查询功能，不需要指定sql，StupidMybatis会自动生成sql
 *
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(ORMSelect.Config.class)
public @interface ORMSelect {
  /**
   * @return 需要查询的DO属性(不是表的字段)，默认为@ORM中的resultMap上所有的属性
   */
  String[] properties() default {};

  final class Config implements MapperConfigHandler<ORMSelect> {

    @Override
    public void handleAnnotation(ORMSelect annotation, Class<?> mapperInterface, Method method, MapperBuilderAssistant assistant) throws Throwable {
      TableMetaCache.ORMConfig ormConfig = getOrmConfig(mapperInterface);
      //注册MappedStatement
      SpecifiedSelectProvider selectProvider = new SpecifiedSelectProvider(SqlProvider.class, "getSql");
      ResultMap resultMap = new SpecifiedResultMap(ormConfig.getOrm().resultMap());
      MapperAnnotationBuilder.parseStatement(assistant, mapperInterface, method, selectProvider, resultMap);
    }

    private TableMetaCache.ORMConfig getOrmConfig(Class<?> mapperInterface) {
      Class<?> resultType = OrmUtils.getOrmEntityClass(mapperInterface);
      TableMetaCache.ORMConfig ormConfig = TableMetaCache.getInstance().getORMConfig(resultType);
      if (ormConfig == null) {
        throw new StupidMybatisOrmException("cannot find orm config for mapperInterface:" + mapperInterface.getName());
      }
      return ormConfig;
    }
  }

  final class SqlProvider {

    public String getSql(Object params, ProviderContext providerContext) {

      Class<?> resultType = OrmUtils.getOrmEntityClass(providerContext.getMapperType());
      if (resultType == null) {
        throw new StupidMybatisOrmException("orm config missed, cannot find result type for " + providerContext.getMapperType().getName());
      }

      TableMetaCache.ORMConfig ormConfig = getOrmConfig(resultType);

      String[] properties = OrmUtils.getProperties(providerContext.getMapperMethod(), ormConfig, ORMSelect.class);
      String[] columns = new String[properties.length];
      for (int i = 0; i < properties.length; i++) {
        columns[i] = ormConfig.getColumn(properties[i]);
      }

      SQL sql = new SQL()
          .SELECT(columns)
          .FROM(ormConfig.getOrm().tableName());

      if (params == null) {
        return sql.toString();
      }

      if (resultType.isAssignableFrom(params.getClass())) {
        for (Map.Entry<String, String> en : ormConfig.getMappings().entrySet()) {
          String field = en.getKey();
          Object value = Reflections.getField(field, params);
          if (value == null) {
            continue;
          }
          sql.WHERE(en.getValue() + " = #{" + field + '}').AND();
        }
      } else if (params instanceof Map) {
        for (Map.Entry<String, ?> en : ((Map<String, ?>) params).entrySet()) {
          String column = ormConfig.getColumn(en.getKey());
          if (column == null) {
            continue;
          }
          sql.WHERE(column + " = #{" + en.getKey() + '}').AND();
        }
      } else {
        throw new StupidMybatisOrmException("unknown parameter map, please add @Param(fieldName) to parameters");
      }
      sql.WHERE("1=1");

      return sql.toString();
    }

  }
}
