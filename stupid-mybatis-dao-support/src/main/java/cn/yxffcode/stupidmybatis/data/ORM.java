package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MybatisConfigUtils;
import cn.yxffcode.stupidmybatis.data.parser.PrimaryKey;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.sql.KeyWord;
import cn.yxffcode.stupidmybatis.data.sql.KeyWordHandler;
import cn.yxffcode.stupidmybatis.data.sql.KeyWords;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
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
      doHandle(orm, type, assistant);
    }

    private void doHandle(ORM orm, Class<?> mapperInterface, MapperBuilderAssistant assistant) throws Throwable {
      //parse keywords
      registKeyWords(mapperInterface);

      String ormResultMap = orm.resultMap();
      if (Strings.isNullOrEmpty(ormResultMap) || Strings.isNullOrEmpty(ormResultMap.trim())) {
        return;
      }

      Method[] methods = mapperInterface.getMethods();


      ResultMap defaultResultMap = assistant.getConfiguration().getResultMap(ormResultMap);
      TableMetaCache.getInstance().parse(orm, defaultResultMap, mapperInterface);

      parseORMStatements(mapperInterface, assistant, methods, defaultResultMap);

      handleKeyWords(mapperInterface, assistant, methods);
    }

    private void parseORMStatements(Class<?> mapperInterface, MapperBuilderAssistant assistant, Method[] methods, ResultMap defaultResultMap) {
      for (Method method : methods) {
        if (shouldSkip(method)) {
          continue;
        }
        doParseORM(assistant, defaultResultMap, method, mapperInterface);
      }
    }

    private void handleKeyWords(Class<?> mapperInterface, MapperBuilderAssistant assistant, Method[] methods) throws Throwable {
      TableMetaCache.ORMConfig ormConfig = OrmUtils.getOrmConfig(OrmUtils.getOrmEntityClass(mapperInterface));
      for (Method method : methods) {
        if (!method.isBridge()) {
          if (assistant.getConfiguration().hasStatement(assistant.getCurrentNamespace() + '.' + method.getName())) {
            KeyWordHandler.getInstance().handleKeyWords(mapperInterface, method, ormConfig, assistant);
          }
        }
      }
    }

    private void registKeyWords(Class<?> mapperInterface) {
      KeyWords keyWords = mapperInterface.getAnnotation(KeyWords.class);
      if (keyWords != null) {
        for (KeyWord keyWord : keyWords.value()) {
          KeyWordHandler.getInstance().registKeyWord(mapperInterface, keyWord);
        }
      }

    }

    private void doParseORM(MapperBuilderAssistant assistant, ResultMap ormResultMap, Method method, Class<?> mapperInterface) {

      String mappedStatementId = assistant.getCurrentNamespace() + '.' + method.getName();
      if (!assistant.getConfiguration().hasStatement(mappedStatementId)) {
        return;
      }
      MappedStatement mappedStatement = assistant.getConfiguration().getMappedStatement(mappedStatementId);

      if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
        return;
      }

      String autoMapResultMapName = MybatisConfigUtils.generateResultMapName(mapperInterface, method);

      if (!assistant.getConfiguration().hasResultMap(autoMapResultMapName)) {
        return;
      }
      ResultMap autoMapResultMap = assistant.getConfiguration().getResultMap(autoMapResultMapName);
      //有映射，则不做默认映射的替换
      if (!autoMapResultMap.getMappedColumns().isEmpty()) {
        return;
      }

      List<ResultMap> resultMaps = mappedStatement.getResultMaps();

      List<ResultMap> newResultMaps = Lists.newArrayListWithCapacity(resultMaps.size());
      for (ResultMap resultMap : resultMaps) {
        if (resultMap == autoMapResultMap) {
          //替换result map
          newResultMaps.add(ormResultMap);
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
