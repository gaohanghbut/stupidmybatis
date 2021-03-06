package cn.yxffcode.stupidmybatis.core;

import com.google.common.collect.Maps;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author gaohang
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring.xml")
public class TypeResultMapTest {

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

    System.out.println("outusers = " + userDao.selectAllMapperTest());
    System.out.println("users = " + userDao.selectById(0));
    System.out.println("users = " + userDao.selectById2(0));
    Map<String, Object> map = Maps.newHashMap();
    map.put("ID", 0);
    map.put("NAME", "hello");
    System.out.println("users = " + userDao.mapToUser(map));

    User param = new User();
    param.setId(0);
    param.setName("name");
    System.out.println("userDao.select = " + userDao.select(param));

  }
}
