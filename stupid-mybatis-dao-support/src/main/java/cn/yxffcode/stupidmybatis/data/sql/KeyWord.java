package cn.yxffcode.stupidmybatis.data.sql;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface KeyWord {
  String name();

  String value() default "";

  Class<? extends SqlContentProvider> contentProvider() default ValueSqlContentProvider.class;

  final class ValueSqlContentProvider implements SqlContentProvider {

    @Override
    public String getContent(KeyWord keyWord, TableMetaCache.ORMConfig ormConfig, MappedStatement mappedStatement) {
      return keyWord.value();
    }
  }
}
