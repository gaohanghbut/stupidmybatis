package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import com.google.common.collect.BiMap;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * @author gaohang
 */
final class BaseDataAccessSqlProvider {

  public String selectById(Object id, ProviderContext providerContext) {
    Class<?> beanType = OrmUtils.getBeanType(providerContext.getMapperType());
    TableMetaCache.ORMConfig ormConfig = getOrmConfig(beanType);

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

  public String insert(Object param) {
    TableMetaCache.ORMConfig ormConfig = getOrmConfig(param.getClass());

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
    TableMetaCache.ORMConfig ormConfig = getOrmConfig(param.getClass());

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

  private void appendWhereClouse(BiMap<String, String> inversedMappings, String[] keyColumns, SQL sql) {
    if (keyColumns.length == 1) {
      sql.WHERE(keyColumns[0] + " = #{" + inversedMappings.get(keyColumns[0]) + '}');
    } else {
      int j = keyColumns.length - 1;
      for (int i = 0; i < j; i++) {
        String keyColumn = keyColumns[i];
        sql.WHERE(keyColumn + " = #{" + inversedMappings.get(keyColumn) + '}').AND();
      }
      sql.WHERE(keyColumns[j] + " = #{" + inversedMappings.get(keyColumns[j]) + '}');
    }
  }

  private TableMetaCache.ORMConfig getOrmConfig(Class<?> beanType) {
    TableMetaCache.ORMConfig ormConfig = TableMetaCache.getInstance().getORMConfig(beanType);
    if (ormConfig == null) {
      throw new StupidMybatisOrmException("no orm config found for " + beanType);
    }
    return ormConfig;
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
