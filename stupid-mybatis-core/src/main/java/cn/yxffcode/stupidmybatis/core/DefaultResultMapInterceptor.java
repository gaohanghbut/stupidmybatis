package cn.yxffcode.stupidmybatis.core;

import com.google.common.collect.Maps;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import static cn.yxffcode.stupidmybatis.core.MappedStatementUtils.copyMappedStatement;

/**
 * 对可复用的{@link org.apache.ibatis.annotations.Results}的支持，如果没有标记{@link org.apache.ibatis.annotations.Results}
 * 或者{@link org.apache.ibatis.annotations.ResultMap}则使用默认的ResultMap
 * <p>
 * 需要将{@link DefaultResults}标记在映射接口上
 *
 * @author gaohang
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
            CacheKey.class, BoundSql.class})
})
public class DefaultResultMapInterceptor implements Interceptor {

  private static final Logger logger = LoggerFactory.getLogger(DefaultResultMapInterceptor.class);

  private ConcurrentMap<String, ResultMapHolder> defaultResultMaps = Maps.newConcurrentMap();

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    final Object[] args = invocation.getArgs();
    final MappedStatement ms = (MappedStatement) args[0];
    final List<ResultMap> resultMaps = ms.getResultMaps();

    final String statementId = ms.getId();
    final int i = statementId.lastIndexOf('.');
    if (i <= 0) {
      return invocation.proceed();
    }
    final String namespace = statementId.substring(0, i);

    final DefaultResults defaultResults = getDefaultResults(namespace);
    if (defaultResults == null) {
      return invocation.proceed();
    }

    if (shouldDoByCurrentResultMaps(resultMaps, defaultResults)) {
      return invocation.proceed();
    }
    //add a default ResultMap
    final ResultMap defaultResultMap = getDefaultResultMap(defaultResults, Class.forName(namespace), namespace, ms);
    if (defaultResultMap == null) {
      //没有默认的ResultMap
      return invocation.proceed();
    }
    //使用新的MappedStatement
    final MappedStatement mappedStatement = copyMappedStatement(ms, defaultResultMap);
    args[0] = mappedStatement;
    return invocation.proceed();
  }

  private boolean shouldDoByCurrentResultMaps(List<ResultMap> resultMaps, DefaultResults defaultResults) {
    if (CollectionUtils.isEmpty(resultMaps)) {
      return false;
    }
    for (int i = 0, j = resultMaps.size(); i < j; i++) {
      final ResultMap resultMap = resultMaps.get(i);
      if (CollectionUtils.isEmpty(resultMap.getMappedColumns()) && resultMap.getType() == defaultResults.resultType()) {
        return false;
      }
    }
    return true;
  }

  private ResultMap getDefaultResultMap(DefaultResults defaultResults, Class<?> mappingInterface, String namespace, MappedStatement ms) {
    final ResultMapHolder resultMapHolder = defaultResultMaps.get(namespace);
    if (resultMapHolder != null) {
      return resultMapHolder.resultMap;
    }
    final ResultMap resultMap = buildResultMap(namespace, ms, mappingInterface, defaultResults);
    defaultResultMaps.putIfAbsent(namespace, new ResultMapHolder(resultMap));
    return resultMap;
  }

  private ResultMap buildResultMap(String namespace, MappedStatement ms, Class<?> mappingInterface,
                                   DefaultResults defaultResults) {
    final MapperBuilderAssistant assistant = new MapperBuilderAssistant(ms.getConfiguration(), ms.getResource());
    final ResultMapBuilder resultMapBuilder = new ResultMapBuilder(assistant, mappingInterface);

    final List<ResultMapping> resultMappings = resultMapBuilder.applyResults(
        defaultResults.results(), defaultResults.resultType());

    final ResultMap.Builder builder = new ResultMap.Builder(ms.getConfiguration(),
        namespace + ".DefaultResultMap", defaultResults.resultType(), resultMappings);
    return builder.build();
  }

  public DefaultResults getDefaultResults(String namespace) {
    try {
      final Class<?> mappingInterface = Class.forName(namespace);
      return mappingInterface.getAnnotation(DefaultResults.class);
    } catch (ClassNotFoundException e) {
      logger.debug("load namespace class failed, maybe namespace {} is not a class", namespace, e);
      return null;
    }
  }

  @Override
  public Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  @Override
  public void setProperties(Properties properties) {
  }

  private static final class ResultMapHolder {

    private static final ResultMapHolder NONE = new ResultMapHolder(null);

    private final ResultMap resultMap;

    private ResultMapHolder(ResultMap resultMap) {
      this.resultMap = resultMap;
    }
  }
}
