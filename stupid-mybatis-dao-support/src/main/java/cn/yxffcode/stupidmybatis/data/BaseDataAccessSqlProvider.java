package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author gaohang
 */
public final class BaseDataAccessSqlProvider {

  public String selectById(Object id, ProviderContext providerContext) {
    Class<?> beanType = OrmUtils.getOrmEntityClass(providerContext.getMapperType());
    TableMetaCache.ORMConfig ormConfig = OrmUtils.getOrmConfig(beanType);

    //column -> property
    BiMap<String, String> mappings = ormConfig.getMappings().inverse();

    String[] columns = new String[mappings.size()];
    mappings.keySet().toArray(columns);

    String[] keyColumns = ormConfig.getOrm().primaryKey().keyColumns();
    SQL sql = new SQL()
        .SELECT(columns)
        .FROM(ormConfig.getOrm().tableName());

    appendWhereClouse(mappings, keyColumns, sql);

    return sql.toString();
  }

  public String conditionSelect(Object param, ProviderContext providerContext) {
    return buildSelectSql(param, providerContext).toString();
  }

  private SQL buildSelectSql(Object param, ProviderContext providerContext) {
    TableMetaCache.ORMConfig ormConfig = OrmUtils.getOrmConfig(OrmUtils.getOrmEntityClass(providerContext.getMapperType()));

    //column -> property
    BiMap<String, String> mappings = ormConfig.getMappings().inverse();

    String[] columns = new String[mappings.size()];
    mappings.keySet().toArray(columns);

    SQL sql = new SQL()
        .SELECT(columns)
        .FROM(ormConfig.getOrm().tableName());


    List<String> conditionColumns = Lists.newArrayList();
    for (String column : columns) {
      String property = mappings.get(column);
      if (Reflections.getField(property, param) != null) {
        conditionColumns.add(column);
      }
    }
    if (conditionColumns.isEmpty()) {
      throw new StupidMybatisOrmException("query " + providerContext.getMapperType().getName() + '.'
          + providerContext.getMapperMethod().getName() + " failed, condition cannot be empty.");
    }

    appendWhereClouse(mappings, conditionColumns, sql);
    return sql;
  }

  public String rangeSelect(Map<String, Object> params, ProviderContext providerContext) {
    TableMetaCache.ORMConfig ormConfig = OrmUtils.getOrmConfig(OrmUtils.getOrmEntityClass(providerContext.getMapperType()));

    SQL sql = null;
    Object equalCondition = params.containsKey("equalCondition") ? params.get("equalCondition") : null;
    Range range = params.containsKey("range") ? (Range) params.get("range") : null;

    if (equalCondition == null && range == null) {
      throw new IllegalArgumentException("range select failed for " + providerContext.getMapperType().getName() + '.'
          + providerContext.getMapperMethod().getName() + " equal condition and range condition cannot be null both");
    }

    if (equalCondition != null) {
      sql = buildSelectSql(equalCondition, providerContext);
    } else {

      //column -> property
      BiMap<String, String> mappings = ormConfig.getMappings().inverse();

      String[] columns = new String[mappings.size()];
      mappings.keySet().toArray(columns);

      sql = new SQL().SELECT(columns).FROM(ormConfig.getOrm().tableName());
    }

    //append range condition
    range.rendSql(sql, params, ormConfig, "range");

    return sql.toString();
  }

  public String insert(Object param) {
    TableMetaCache.ORMConfig ormConfig = OrmUtils.getOrmConfig(param.getClass());

    int columnCount = ormConfig.getMappings().size();
    if (ormConfig.getOrm().primaryKey().autoGenerate()) {
      columnCount--;
    }
    String[] columns = new String[columnCount];
    String[] properties = new String[columnCount];
    int idx = 0;
    for (Map.Entry<String, String> en : ormConfig.getMappings().entrySet()) {
      columns[idx] = en.getValue();
      properties[idx++] = "#{" + en.getKey() + "}";
    }
    return new SQL()
        .INSERT_INTO(ormConfig.getOrm().tableName())
        .INTO_COLUMNS(columns)
        .INTO_VALUES(properties)
        .toString();
  }

  public String update(Object param) {
    TableMetaCache.ORMConfig ormConfig = OrmUtils.getOrmConfig(param.getClass());

    BiMap<String, String> mappings = ormConfig.getMappings();
    String[] keyColumns = ormConfig.getOrm().primaryKey().keyColumns();

    SQL sql = new SQL().UPDATE(ormConfig.getOrm().tableName());
    for (Map.Entry<String, String> en : mappings.entrySet()) {
      String column = en.getValue();
      if (isKeyColumn(keyColumns, column)) {
        continue;
      }
      sql.SET(column + " =#{" + en.getKey() + '}');
    }
    //append primary key
    appendWhereClouse(mappings.inverse(), keyColumns, sql);
    return sql.toString();
  }

  private void appendWhereClouse(BiMap<String, String> inversedMappings, String[] conditionColumns, SQL sql) {
    appendWhereClouse(inversedMappings, Arrays.asList(conditionColumns), sql);
  }

  private void appendWhereClouse(BiMap<String, String> inversedMappings, List<String> conditionColumns, SQL sql) {
    if (conditionColumns.size() == 1) {
      sql.WHERE(conditionColumns.get(0) + " = #{" + inversedMappings.get(conditionColumns.get(0)) + '}');
    } else {
      int j = conditionColumns.size() - 1;
      for (int i = 0; i < j; i++) {
        String conditionColumn = conditionColumns.get(i);
        sql.WHERE(conditionColumn + " = #{" + inversedMappings.get(conditionColumn) + '}').AND();
      }
      sql.WHERE(conditionColumns.get(j) + " = #{" + inversedMappings.get(conditionColumns.get(j)) + '}');
    }
  }

  private boolean isKeyColumn(String[] keyColumns, String column) {
    for (String keyColumn : keyColumns) {
      if (keyColumn.equals(column)) {
        return true;
      }
    }
    return false;
  }
}
