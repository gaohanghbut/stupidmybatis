package cn.yxffcode.stupidmybatis.core;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;

import java.lang.reflect.Field;
import java.util.Collections;

/**
 * @author gaohang on 16/3/3.
 */
public final class MappedStatementUtils {
  private MappedStatementUtils() {
  }

  public static MappedStatement copyMappedStatement(MappedStatement ms, BoundSql boundSql) {
    return copyMappedStatement(ms, new BoundSqlSource(boundSql));
  }

  public static MappedStatement copyMappedStatement(MappedStatement ms, SqlSource sqlSource) {
    MappedStatement nms;
    nms = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), sqlSource,
        ms.getSqlCommandType()).cache(ms.getCache()).databaseId(ms.getDatabaseId())
        .fetchSize(ms.getFetchSize()).flushCacheRequired(true).keyGenerator(ms.getKeyGenerator())
        .parameterMap(ms.getParameterMap()).resource(ms.getResource())
        .resultMaps(ms.getResultMaps()).resultSetType(ms.getResultSetType())
        .statementType(ms.getStatementType()).timeout(ms.getTimeout()).useCache(ms.isUseCache())
        .build();
    setField(nms, "keyColumns", ms.getKeyColumns());
    setField(nms, "keyProperties", ms.getKeyProperties());
    return nms;
  }

  public static MappedStatement copyMappedStatement(MappedStatement ms, ResultMap resultMap) {
    MappedStatement nms;
    nms = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), ms.getSqlSource(),
        ms.getSqlCommandType()).cache(ms.getCache()).databaseId(ms.getDatabaseId())
        .fetchSize(ms.getFetchSize()).flushCacheRequired(true).keyGenerator(ms.getKeyGenerator())
        .parameterMap(ms.getParameterMap()).resource(ms.getResource())
        .resultMaps(Collections.singletonList(resultMap)).resultSetType(ms.getResultSetType())
        .statementType(ms.getStatementType()).timeout(ms.getTimeout()).useCache(ms.isUseCache())
        .build();
    setField(nms, "keyColumns", ms.getKeyColumns());
    setField(nms, "keyProperties", ms.getKeyProperties());
    return nms;
  }

  public static void setField(Object target, String field, Object value) {
    Field ss = findField(target.getClass(), field);
    ss.setAccessible(true);
    setField(ss, target, value);
  }

  private static Field findField(Class<?> clazz, String name) {
    return findField(clazz, name, null);
  }

  /**
   * Attempt to find a {@link Field field} on the supplied {@link Class} with the
   * supplied {@code name} and/or {@link Class type}. Searches all superclasses
   * up to {@link Object}.
   *
   * @param clazz the class to introspect
   * @param name  the name of the field (may be {@code null} if type is specified)
   * @param type  the type of the field (may be {@code null} if name is specified)
   * @return the corresponding Field object, or {@code null} if not found
   */
  public static Field findField(Class<?> clazz, String name, Class<?> type) {
    Class<?> searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Field[] fields = searchType.getDeclaredFields();
      for (Field field : fields) {
        if ((name == null || name.equals(field.getName())) && (type == null || type
            .equals(field.getType()))) {
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  public static void setField(Field field, Object target, Object value) {
    try {
      field.set(target, value);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException(
          "Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage()
          , ex);
    }
  }
}
