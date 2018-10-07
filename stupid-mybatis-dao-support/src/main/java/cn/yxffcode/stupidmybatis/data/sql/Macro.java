package cn.yxffcode.stupidmybatis.data.sql;

import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import org.apache.ibatis.mapping.MappedStatement;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Macro {
  /**
   * @return 关键词名
   */
  String name();

  /**
   * @return 关键词的内容，如果没有指定{@link #contentProvider()}则使用value的值
   */
  String value() default "";

  /**
   * @return 处理关键词，替换为sql中的内容的SqlContentProvider
   */
  Class<? extends SqlContentProvider> contentProvider() default ValueSqlContentProvider.class;

  final class ValueSqlContentProvider implements SqlContentProvider {

    @Override
    public String getContent(Macro macro, TableMetaCache.ORMConfig ormConfig, MappedStatement mappedStatement) {
      return macro.value();
    }
  }
}
