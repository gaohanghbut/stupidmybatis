package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import cn.yxffcode.stupidmybatis.data.cfg.SpecifiedInsertProvider;
import cn.yxffcode.stupidmybatis.data.parser.MapperAnnotationBuilder;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.jdbc.SQL;

import java.lang.annotation.*;
import java.lang.reflect.Method;

import static cn.yxffcode.stupidmybatis.data.utils.OrmUtils.getOrmConfig;

/**
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(ORMInsert.Config.class)
public @interface ORMInsert {
  /**
   * @return 需要查询的DO属性(不是表的字段)，默认为@ORM中的resultMap上所有的属性
   */
  String[] properties() default {};

  final class Config implements MapperConfigHandler<ORMInsert> {

    @Override
    public void handleAnnotation(ORMInsert annotation, Class<?> mapperInterface, Method method, MapperBuilderAssistant assistant) throws Throwable {
      //注册MappedStatement
      SpecifiedInsertProvider insertProvider = new SpecifiedInsertProvider(SqlProvider.class, "getSql");
      MapperAnnotationBuilder.parseStatement(assistant, mapperInterface, method, insertProvider, null);
    }
  }

  final class SqlProvider {

    public String getSql(Object params, ProviderContext providerContext) {

      Class<?> resultType = OrmUtils.getOrmEntityClass(providerContext.getMapperType());
      if (resultType == null) {
        throw new StupidMybatisOrmException("orm config missed, cannot find result type for " + providerContext.getMapperType().getName());
      }

      TableMetaCache.ORMConfig ormConfig = getOrmConfig(resultType);

      String[] properties = OrmUtils.getProperties(providerContext.getMapperMethod(), ormConfig, ORMInsert.class);
      String[] columns = new String[properties.length];
      for (int i = 0; i < properties.length; i++) {
        String property = properties[i];
        properties[i] = "#{" + property + '}';
        columns[i] = ormConfig.getColumn(property);
      }
      return new SQL()
          .INSERT_INTO(ormConfig.getOrm().tableName())
          .INTO_COLUMNS(columns)
          .INTO_VALUES(properties)
          .toString();
    }

  }
}
