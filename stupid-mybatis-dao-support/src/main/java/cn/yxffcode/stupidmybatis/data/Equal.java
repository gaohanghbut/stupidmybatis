package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * @author gaohang
 */
public class Equal implements Range {

  private final String rangeField;
  private final Object value;
  /**
   * 是否!=，默认为false
   */
  private final boolean inverse;

  public Equal(String rangeField, Object value) {
    this(rangeField, value, false);
  }

  public Equal(String rangeField, Object value, boolean inverse) {
    this.rangeField = rangeField;
    this.value = value;
    this.inverse = inverse;
  }

  public String getRangeField() {
    return rangeField;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public void rendSql(SQL sql, Map<String, Object> params, TableMetaCache.ORMConfig ormConfig, String genericRangeName) {
    String rangeColumn = ormConfig.getColumn(this.rangeField);
    if (inverse) {
      sql.WHERE(rangeColumn + " != #{" + genericRangeName + ".value}");
    } else {
      sql.WHERE(rangeColumn + " = #{" + genericRangeName + ".value}");
    }
  }
}
