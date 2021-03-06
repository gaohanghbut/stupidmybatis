package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.data.cfg.SpecifiedResultMap;
import cn.yxffcode.stupidmybatis.data.cfg.SpecifiedSelectProvider;
import cn.yxffcode.stupidmybatis.data.parser.MapperAnnotationBuilder;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import com.google.common.base.Strings;
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

  /**
   * @return 参数前缘，比如paramPrefix="user"，则参数为 #{user.id}，#{user.name}
   */
  String paramPrefix() default "";

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

      if (params != null) {
        String paramPrefix = providerContext.getMapperMethod().getDeclaredAnnotation(ORMSelect.class).paramPrefix();
        appendConditions(params, paramPrefix, resultType, ormConfig, sql);
      }
      //group by
      appendGroupBy(providerContext, sql, ormConfig);

      //order by
      appendOrderBy(providerContext, sql, ormConfig);

      //limit
      Limitation limitation = providerContext.getMapperMethod().getAnnotation(Limitation.class);
      if (limitation == null) {
        return sql.toString();
      }

      StringBuilder sb = new StringBuilder(sql.toString());
      sb.append(" limit ");
      if (!Strings.isNullOrEmpty(limitation.offsetParam())) {
        sb.append(" #{").append(limitation.offsetParam()).append('}').append(',');
      }
      sb.append(" #{").append(limitation.limitParam()).append('}');

      return sb.toString();
    }

    private void appendOrderBy(ProviderContext providerContext, SQL sql, TableMetaCache.ORMConfig ormConfig) {
      OrderBy orderBy = providerContext.getMapperMethod().getAnnotation(OrderBy.class);
      if (orderBy != null) {
        OrderBy.Order[] orders = orderBy.value();
        for (OrderBy.Order order : orders) {
          sql.ORDER_BY(ormConfig.getColumn(order.value()) + ' ' + order.sort());
        }
      }
    }

    private void appendGroupBy(ProviderContext providerContext, SQL sql, TableMetaCache.ORMConfig ormConfig) {
      GroupBy groupBy = providerContext.getMapperMethod().getAnnotation(GroupBy.class);
      if (groupBy != null) {
        for (String prop : groupBy.value()) {
          sql.GROUP_BY(ormConfig.getColumn(prop));
        }
      }
    }

    private void appendConditions(Object params, String paramPrefix, Class<?> resultType, TableMetaCache.ORMConfig ormConfig, SQL sql) {
      if (resultType.isAssignableFrom(params.getClass())) {
        for (Map.Entry<String, String> en : ormConfig.getMappings().entrySet()) {
          String field = en.getKey();
          Object value = Reflections.getField(field, params);
          if (value == null) {
            continue;
          }
          if (Strings.isNullOrEmpty(paramPrefix)) {
            sql.WHERE(en.getValue() + " = #{" + field + '}').AND();
          } else {
            sql.WHERE(en.getValue() + " = #{" + paramPrefix + '.' + field + '}').AND();
          }
        }
      } else if (params instanceof Map) {
        for (Map.Entry<String, ?> en : ((Map<String, ?>) params).entrySet()) {
          String column = ormConfig.getColumn(en.getKey());
          if (column == null) {
            continue;
          }
          if (Strings.isNullOrEmpty(paramPrefix)) {
            sql.WHERE(column + " = #{" + en.getKey() + '}').AND();
          } else {
            sql.WHERE(column + " = #{" + paramPrefix + '.' + en.getKey() + '}').AND();
          }
        }
      } else {
        throw new StupidMybatisOrmException("unknown parameter map, please add @Param(fieldName) to parameters");
      }
      sql.WHERE("1=1");
    }

  }
}
