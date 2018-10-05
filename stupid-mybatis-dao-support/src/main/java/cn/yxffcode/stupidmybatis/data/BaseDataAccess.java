package cn.yxffcode.stupidmybatis.data;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * @author gaohang
 */
public interface BaseDataAccess<DO, ID> {

  /**
   * 插入数据
   */
  @InsertProvider(type = BaseDataAccessSqlProvider.class, method = "insert")
  int insert(DO object);

  /**
   * 批量插入
   */
  @InsertProvider(type = BaseDataAccessSqlProvider.class, method = "insert")
  int batchInsert(List<DO> objects);

  /**
   * 更新数据
   */
  @UpdateProvider(type = BaseDataAccessSqlProvider.class, method = "update")
  int update(DO object);

  /**
   * 批量更新数据
   */
  @UpdateProvider(type = BaseDataAccessSqlProvider.class, method = "update")
  int batchUpdate(List<DO> objects);

  /**
   * 通过id查询
   */
  @SelectProvider(type = BaseDataAccessSqlProvider.class, method = "selectById")
  DO selectById(ID id);

}
