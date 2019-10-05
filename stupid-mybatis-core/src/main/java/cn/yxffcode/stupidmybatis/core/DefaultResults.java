package cn.yxffcode.stupidmybatis.core;

import org.apache.ibatis.annotations.Result;

import java.lang.annotation.*;

/**
 * 默认的ResultMap
 *
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DefaultResults {

  Class<?> resultType();

  Result[] results();
}
