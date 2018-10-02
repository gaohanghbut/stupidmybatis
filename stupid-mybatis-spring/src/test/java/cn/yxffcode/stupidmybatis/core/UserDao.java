package cn.yxffcode.stupidmybatis.core;

import cn.yxffcode.stupidmybatis.core.statement.TypeResultMap;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author gaohang on 16/7/29.
 */
public interface UserDao {

  @Insert({
      "insert into user (id, name) values (#{id}, #{name})"
  })
  int batchInsert(List<User> users);

  @Select({
      "select id, name from user"
  })
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  List<User> selectAll();

  @Select({
      "select id, name from user"
  })
  @ResultMap("userMapper")
  List<User> selectAllMapperTest();

  @Select("select id, name from user where id = #{id}")
  @ResultMap("userMapper")
  User selectById(@Param("id") int id);

  @TypeResultMap({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  User userMapper();
}
