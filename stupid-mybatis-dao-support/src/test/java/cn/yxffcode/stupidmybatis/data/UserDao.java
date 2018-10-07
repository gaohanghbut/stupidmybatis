package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.core.statement.TypeResultMap;
import cn.yxffcode.stupidmybatis.data.parser.PrimaryKey;
import cn.yxffcode.stupidmybatis.data.sql.KeyWord;
import cn.yxffcode.stupidmybatis.data.sql.KeyWords;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author gaohang on 16/7/29.
 */
@TypeResultMap(id = "userResultMap", resultType = User.class, value = {
    @Result(property = "id", column = "id"),
    @Result(property = "name", column = "name_t")
})
@ORM(tableName = "user", resultMap = "userResultMap", primaryKey = @PrimaryKey(keyColumns = "id", autoGenerate = false))
@KeyWords({
    @KeyWord(name = "statementId", contentProvider = StatementIdSqlContentProvider.class),
    @KeyWord(name = "notDeleted", value = "status != 0")
})
public interface UserDao extends BaseDataAccess<User, Integer> {

  @ORMSelect
  List<User> selectAll();

  @ORMSelect
  @OrderBy(@OrderBy.Order(value = "id", sort = OrderBy.Sort.DESC))
  @GroupBy({"name", "id"})
  @Limitation(offsetParam = "offset", limitParam = "limit")
  List<User> selectPage2(@Param("offset") int offset, @Param("limit") int limit);

  @Select("select /* @statementId */ @columns from user where @notDeleted order by id limit #{offset}, #{limit}")
  List<User> selectPage(@Param("offset") int offset, @Param("limit") int limit);

  @ORMInsert
  int insertUser(User user);

  @ORMUpdate
  int updateUser(User user);

  @ORMUpdate
  int updateUserByParams(@Param("id") int id, @Param("name") String name);

  @ORMSelect
  User selectByName(@Param("name") String name);

  @ORMSelect
  List<User> selectByParams(@Param("id") int id, @Param("name") String name);

  @ORMSelect
  List<User> selectByUserParams(User user);

  @ORMSelect(properties = "name")
  List<String> selectNames(@Param("id") int id);

  @ORMDelete
  int deleteUser(@Param("id") int id);

  @ORMDelete(conditions = "name")
  int deleteUserByName(@Param("name") String name);

}
