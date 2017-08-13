package cn.yxffcode.stupidmybatis.core;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 一次绑定的SqlSource实现,{@link #getBoundSql(Object)}始终返回相同的对象,
 * 在解析SQL前需要获取BoundSql,获取后使用此类对象临时代替原始SqlSource,保证在
 * 一次DB访问中,SQL只被绑定一次.
 *
 * @author gaohang on 15/12/30.
 */
public class BoundSqlSource implements SqlSource {
  private BoundSql boundSql;

  public BoundSqlSource(BoundSql boundSql) {
    this.boundSql = boundSql;
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return boundSql;
  }
}
