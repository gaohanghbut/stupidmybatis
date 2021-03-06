package cn.yxffcode.stupidmybatis.data.sql;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * @author gaohang
 */
public interface SqlContentProvider {
  String getContent(Macro macro, TableMetaCache.ORMConfig ormConfig, MappedStatement mappedStatement);
}
