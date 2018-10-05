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

  }
}
