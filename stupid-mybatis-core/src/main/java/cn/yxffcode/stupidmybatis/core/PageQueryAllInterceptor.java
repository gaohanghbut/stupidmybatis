package cn.yxffcode.stupidmybatis.core;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author gaohang on 16/8/4.
 */
@Intercepts({@Signature(type = Executor.class, method = "query",
    args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
            CacheKey.class, BoundSql.class}), @Signature(type = Executor.class, method = "update",
    args = {MappedStatement.class, Object.class})})
public class PageQueryAllInterceptor implements Interceptor {
  @Override
  public Object intercept(final Invocation invocation) throws Throwable {
    final Paged paged = DaoQueryPageContextHolder.get();
    if (paged == null) {
      return invocation.proceed();
    }
    final MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
    final BoundSql boundSql = ms.getBoundSql(invocation.getArgs()[1]);
    final String originalSql = boundSql.getSql();

    List<Object> result = new ArrayList<>();
    final int pageSize = paged.value();
    int off = 0;
    while (true) {
      final String newSql = new StringBuilder(originalSql)
          .append(" limit ")
          .append(off)
          .append(' ')
          .append(pageSize).toString();
      Reflections.setField(boundSql, "sql", newSql);
      invocation.getArgs()[0] = MappedStatementUtils.copyMappedStatement(ms, boundSql);
      final List<?> requestDb = (List<?>) invocation.proceed();
      result.addAll(requestDb);
      if (requestDb.size() < pageSize) {
        break;
      }
      off += requestDb.size();
    }

    return result;
  }

  @Override
  public Object plugin(final Object target) {
    if (target instanceof Executor) {
      return Plugin.wrap(target, this);
    }
    return target;
  }

  @Override
  public void setProperties(final Properties properties) {
  }
}
