package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 条件范围查询对象
 *
 * @author gaohang
 */
public class ConditionRange implements Range {
  private final String rangeField;
  private final Object minValue;
  private final boolean includeMinValue;
  private final Object maxValue;
  private final boolean includeMaxValue;

  public ConditionRange(String rangeField, Object minValue, Object maxValue) {
    this(rangeField, minValue, true, maxValue, false);
  }

  public ConditionRange(String rangeField, Object minValue, boolean includeMinValue, Object maxValue, boolean includeMaxValue) {
    this.rangeField = rangeField;
    this.minValue = minValue;
    this.includeMinValue = includeMinValue;
    this.maxValue = maxValue;
    this.includeMaxValue = includeMaxValue;
  }

  /**
   * @return 范围查询的字段，需要是DO中的字段，不是表的column
   */
  String getRangeField() {
    return rangeField;
  }

  /**
   * @return rangeField的超始值
   */
  Object getMinValue() {
    return minValue;
  }

  /**
   * @return 是否包含起始值
   */
  boolean includeMinValue() {
    return includeMinValue;
  }

  /**
   * @return rangeField的最大值
   */
  Object getMaxValue() {
    return maxValue;
  }

  /**
   * @return 是否包含rangeField的最大值
   */
  boolean includeMaxValue() {
    return includeMaxValue;
  }

  @Override
  public void rendSql(SQL sql, Map<String, Object> params, TableMetaCache.ORMConfig ormConfig, String genericRangeName) {
    String rangeColumn = ormConfig.getColumn(this.rangeField);

    if (includeMinValue) {
      sql.WHERE(rangeColumn + " >= #{" + genericRangeName + ".minValue}");
    } else {
      sql.WHERE(rangeColumn + " > #{" + genericRangeName + ".minValue}");
    }
    sql.AND();
    if (includeMaxValue) {
      sql.WHERE(rangeColumn + " <= #{" + genericRangeName + ".maxValue}");
    } else {
      sql.WHERE(rangeColumn + " < #{" + genericRangeName + ".maxValue}");
    }
  }
}
