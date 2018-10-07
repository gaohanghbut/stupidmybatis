package cn.yxffcode.stupidmybatis.data;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
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
   * 更新数据，忽略null属性
   */
  @ORMUpdate(ignoreNull = true)
  int updateIgnoreNull(DO object);

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

  /**
   * 条件查询
   *
   * @param condition 查询条件
   * @return 查询结果
   */
  @SelectProvider(type = BaseDataAccessSqlProvider.class, method = "conditionSelect")
  List<DO> select(DO condition);

  /**
   * 范围查询，带上=参数
   *
   * @param condition 需要相等的条件参数
   * @param range     范围参数
   */
  @SelectProvider(type = BaseDataAccessSqlProvider.class, method = "rangeSelect")
  List<DO> selectFixedRange(@Param("equalCondition") DO condition, @Param("range") Range range);

  /**
   * 范围查询
   */
  @SelectProvider(type = BaseDataAccessSqlProvider.class, method = "rangeSelect")
  List<DO> selectRange(@Param("range") Range range);

}
