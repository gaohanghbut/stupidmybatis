package cn.yxffcode.stupidmybatis.core;

import cn.yxffcode.stupidmybatis.core.statement.MapperMethod;
import cn.yxffcode.stupidmybatis.core.statement.TypeResultMap;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

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

  @Select("select id, name from user where id = #{id}")
  @MapperMethod("mapToUser")
  @ResultMap("selectById2")
  User selectById2(@Param("id") int id);

  @TypeResultMap({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  User userMapper();

  /**
   * @param result
   * @return
   */
  default User mapToUser(Map<String, ?> result) {
    if (result == null) {
      return null;
    }
    User user = new User();
    user.setId((Integer) result.get("ID"));
    user.setName((String) result.get("NAME"));
    return user;
  }

}
