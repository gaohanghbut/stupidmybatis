package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 表示查询的范围
 * @author gaohang
 */
public interface Range {

  /**
   * 拼接SQL
   * @param sql sql对象
   * @param params 参数
   * @param genericRangeName 生成的range参数名
   */
  void rendSql(SQL sql, Map<String, Object> params, TableMetaCache.ORMConfig ormConfig, String genericRangeName);

}
