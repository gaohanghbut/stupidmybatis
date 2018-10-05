package cn.yxffcode.stupidmybatis.data.parser;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MybatisConfigUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(ORM.Parser.class)
public @interface ORM {

  /**
   * @return 表名
   */
  String tableName();

  /**
   * @return 主键配置
   */
  PrimaryKey primaryKey() default @PrimaryKey(keyColumns = "id", autoGenerate = true);

  /**
   * @return 结果映射
   */
  String resultMap() default "";

  final class Parser implements MapperConfigHandler<ORM> {

    @Override
    public void handleAnnotation(ORM orm, Class<?> type, Method mtd, MapperBuilderAssistant assistant) throws Throwable {
      String ormResultMap = orm.resultMap();
      if (Strings.isNullOrEmpty(ormResultMap) || Strings.isNullOrEmpty(ormResultMap.trim())) {
        return;
      }

      for (Method method : type.getMethods()) {
        if (shouldSkip(method)) {
          continue;
        }
        doParseORM(orm, assistant, ormResultMap, method, type);
      }
      Class<?>[] interfaces = type.getInterfaces();
      if (interfaces == null || interfaces.length == 0) {
        return;
      }
      for (Class<?> newType : interfaces) {
        handleAnnotation(orm, newType, null, assistant);
      }
    }

    private void doParseORM(ORM orm, MapperBuilderAssistant assistant, String ormResultMap, Method method, Class<?> type) {

      String mappedStatementId = assistant.getCurrentNamespace() + '.' + method.getName();
      if (!assistant.getConfiguration().hasStatement(mappedStatementId)) {
        return;
      }
      MappedStatement mappedStatement = assistant.getConfiguration().getMappedStatement(mappedStatementId);

      if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
        return;
      }

      String autoMapResultMapName = MybatisConfigUtils.generateResultMapName(type, method);

      if (!assistant.getConfiguration().hasResultMap(autoMapResultMapName)) {
        return;
      }
      ResultMap autoMapResultMap = assistant.getConfiguration().getResultMap(autoMapResultMapName);
      //有映射，则不做默认映射的替换
      if (!autoMapResultMap.getMappedColumns().isEmpty()) {
        return;
      }

      ResultMap actualResultMap = assistant.getConfiguration().getResultMap(ormResultMap);
      TableMetaCache.getInstance().parse(orm, actualResultMap, type);


      List<ResultMap> resultMaps = mappedStatement.getResultMaps();

      List<ResultMap> newResultMaps = Lists.newArrayListWithCapacity(resultMaps.size());
      for (ResultMap resultMap : resultMaps) {
        if (resultMap == autoMapResultMap) {
          //替换result map
          newResultMaps.add(actualResultMap);
        } else {
          newResultMaps.add(resultMap);
        }
      }
      Reflections.setField(mappedStatement, "resultMaps", newResultMaps);
    }

    private boolean shouldSkip(Method method) {
      Results results = method.getAnnotation(Results.class);
      org.apache.ibatis.annotations.ResultMap resultMapAnnotation = method.getAnnotation(org.apache.ibatis.annotations.ResultMap.class);
      if (results != null || resultMapAnnotation != null) {
        return true;
      }
      return false;
    }
  }
}
