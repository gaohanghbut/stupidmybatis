package cn.yxffcode.stupidmybatis.data.parser;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface PrimaryKey {

  /**
   * @return 主键字段
   */
  String[] keyColumns();

  /**
   * @return 是否是数据库自动生成的key
   */
  boolean autoGenerate();
}
