package cn.yxffcode.stupidmybatis.springboot;

import cn.yxffcode.stupidmybatis.core.statement.MapperMethod;
import cn.yxffcode.stupidmybatis.core.statement.TypeResultMap;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * @author gaohang on 16/7/29.
 */
@TypeResultMap(id = "userMapper2", resultType = User.class, value = {
    @Result(property = "id", column = "id"),
    @Result(property = "name", column = "name")
})
@Mapper
public interface UserDao {

  @Insert("insert into user (id, name) values (#{id}, #{name})")
  int batchInsert(List<User> users);

  @Select("select id, name from user")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  List<User> selectAll();

  @Select({
      "<script>",
        "select id, name from user",
        "<where>",
          "<if test='id != null'>",
            "id = #{id}",
          "</if>",
        "</where>",
      "</script>"
  })
  List<User> select(User user);

  @Select("select id, name from user")
  @MapperMethod("mapToUser")
  List<User> selectAllMapperTest();

  @Select("select id, name from user where id = #{id}")
  @ResultMap("userMapper2")
  User selectById(@Param("id") int id);

  @Select("select id, name from user where id = #{id}")
  @ResultMap("userMapper")
  @MapperMethod("userTransform")
  User selectById2(@Param("id") int id);

  @TypeResultMap({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  User userMapper();

  @TypeResultMap({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  Map mapMapper();

  default User mapToUser(Map<String, ?> result) {
    if (result == null) {
      return null;
    }
    User user = new User();
    user.setId((Integer) result.get("ID"));
    user.setName((String) result.get("NAME"));
    return user;
  }

  default User userTransform(User user) {
    if (user == null) {
      return null;
    }
    User u = new User();
    user.setId(user.getId());
    user.setName("hello " + user.getName());
    return user;
  }

}
