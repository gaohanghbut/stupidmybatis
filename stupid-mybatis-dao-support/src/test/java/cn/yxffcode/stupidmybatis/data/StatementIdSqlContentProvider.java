package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.sql.KeyWord;
import cn.yxffcode.stupidmybatis.data.sql.SqlContentProvider;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * @author gaohang
 */
public class StatementIdSqlContentProvider implements SqlContentProvider {
  @Override
  public String getContent(KeyWord keyWord, TableMetaCache.ORMConfig ormConfig, MappedStatement mappedStatement) {
    return mappedStatement.getId();
  }
}
