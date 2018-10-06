package cn.yxffcode.stupidmybatis.data;

import cn.yxffcode.stupidmybatis.core.statement.TypeResultMap;
import cn.yxffcode.stupidmybatis.data.parser.PrimaryKey;
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
public interface UserDao extends BaseDataAccess<User, Integer> {

  @Select("select id, name_t from user")
  List<User> selectAll();

}
