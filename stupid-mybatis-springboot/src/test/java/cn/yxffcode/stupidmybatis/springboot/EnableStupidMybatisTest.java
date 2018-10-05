package cn.yxffcode.stupidmybatis.springboot;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;

/**
 * @author gaohang
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@MapperScan(basePackages = "cn.yxffcode.stupidmybatis.springboot")
@SpringBootApplication
@EnableStupidMybatis
public class EnableStupidMybatisTest {

  @Autowired
  private UserDao userDao;

  @Test
  public void test() {

    User user = new User();
    user.setId(0);
    user.setName("test");
    userDao.batchInsert(Collections.singletonList(user));

    System.out.println("outusers = " + userDao.selectAllMapperTest());
    System.out.println("users = " + userDao.selectAll());
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
