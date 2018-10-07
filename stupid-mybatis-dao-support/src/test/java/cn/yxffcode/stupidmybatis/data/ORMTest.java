package cn.yxffcode.stupidmybatis.data;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;

/**
 * @author gaohang
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring.xml")
public class ORMTest {

  @Resource
  private SqlSessionFactory sqlSessionFactory;

  @Test
  public void test() throws IOException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserDao userDao = sqlSession.getMapper(UserDao.class);

    User user = new User();
    user.setId(0);
    user.setName("test");
    userDao.batchInsert(Collections.singletonList(user));
    user.setId(1);
    userDao.insert(user);

    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    System.out.println("userDao.selectById() = " + userDao.selectById(0));

    user.setName("joh");
    System.out.println("userDao.update() = " + userDao.update(user));
    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    user.setName("john");
//    System.out.println("userDao.update() = " + userDao.update(user));
    System.out.println("userDao.batchUpdate() = " + userDao.batchUpdate(Collections.singletonList(user)));
    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    System.out.println("userDao.select() = " + userDao.select(user));

    ConditionRange conditionRange = new ConditionRange("id", 0, 100);
    System.out.println("userDao.rangeSelect() = " + userDao.selectRange(conditionRange));
    Range range = new LogicRange(conditionRange, LogicRange.Logic.AND, new ConditionRange("name", "a", "z"));
    System.out.println("userDao.rangeSelect() = " + userDao.selectRange(range));
    System.out.println("userDao.selectByName() = " + userDao.selectByName("test"));
    System.out.println("userDao.selectByParams() = " + userDao.selectByParams(0, "test"));
    System.out.println("userDao.selectByUserParams() = " + userDao.selectByUserParams(user));
    user.setId(2);
    userDao.insertUser(user);
    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    user.setName("this is new name");
    userDao.updateUser(user);
    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    userDao.updateUserByParams(2, "this is sec new name");
    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    System.out.println("userDao.selectNames() = " + userDao.selectNames(2));
    System.out.println("userDao.deleteUser() = " + userDao.deleteUser(2));
    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    System.out.println("userDao.deleteUserByName() = " + userDao.deleteUserByName("test"));
    System.out.println("userDao.selectAll() = " + userDao.selectAll());
    System.out.println("userDao.selectPage2() = " + userDao.selectPage2(0, 100));
    System.out.println("userDao.selectPage() = " + userDao.selectPage(0, 100));
  }
}
