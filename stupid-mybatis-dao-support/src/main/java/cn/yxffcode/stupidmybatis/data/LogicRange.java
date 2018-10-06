package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

/**
 * 逻辑表达式范围查询
 *
 * @author gaohang
 */
public class LogicRange implements Range {

  private final Range left;
  private final Logic logic;
  private final Range right;

  public LogicRange(Range left, Logic logic, Range right) {
    this.left = left;
    this.logic = logic;
    this.right = right;
  }

  @Override
  public void rendSql(SQL sql, Map<String, Object> params, TableMetaCache.ORMConfig ormConfig, String genericRangeName) {
    SQL subSql = new SQL();
    String leftRangeName = genericRangeName + "_left";
    params.put(leftRangeName, left);
    left.rendSql(subSql, params, ormConfig, leftRangeName);
    switch (logic) {
      case OR:
        subSql.OR();
        break;
      case AND:
        subSql.AND();
        break;
      default:
        throw new UnsupportedOperationException("logic express " + logic + " is not supported");
    }
    String rightRangeName = genericRangeName + "_right";
    params.put(rightRangeName, right);
    right.rendSql(subSql, params, ormConfig, rightRangeName);
    String conditions = getRangeConditionSql(subSql);
    sql.WHERE(conditions);
  }

  private String getRangeConditionSql(SQL subSql) {
    Object sqlStatement = Reflections.getField("sql", subSql);
    List<String> whereClauses = (List<String>) Reflections.getField("where", sqlStatement);

    StringBuilder conditions = new StringBuilder();
    conditions.append('(');
    for (String whereClause : whereClauses) {
      conditions.append(whereClause);
    }
    conditions.append(')');
    return conditions.toString();
  }

  enum Logic {
    AND, OR
  }
}
