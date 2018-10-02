# StupidMybatis
StupidMybatis是一个mybatis扩展框架，用于简化使用mybatis的过程中的一些痛点，详细见下文。

使用StupidMybatis，先将spring中的SqlSessionFactoryBean替换成StupidSqlSessionFactoryBean,使用方式与SqlSessionFactory相同，例如：
```xml
  <bean id = "sqlSessionFactory" class="cn.yxffcode.stupidmybatis.spring.StupidSqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configLocation" value="classpath:mybatis-config.xml"/>
  </bean>
```

如果不使用spring，则需要将SqlSessionFactoryBuilder替换成StupidSqlSessionFactoryBuilder,例如：
```java
String resource = "mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new StupidSqlSessionFactoryBuilder().build(inputStream);
```

## 可复用的Result注解
mybatis提供的@esults注解只能标记在声明注解的方法上，如果有多个查询方法需要使用到相同的@Results注解，
只能再次配置一遍@Result或者使用xml，stupidmybatis提供了@TypeResultMap，可使得通过注解配置的Result
可复用

### 使用方式

通过@Result配置的代码如下：
```java
public interface UserDao {

  @Select({
      "select id, name from user"
  })
  //p定义resultmap
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  List<User> selectAll();

  @Select("select id, name from user where id = #{id}")
  //再次定义相同的resultmap
  @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name")
    })
    List<User> selectAll();
  User selectById(@Param("id") int id);
}

```

可以看到需要重复定义@Results，当使用@TypeResultMap后的代码：
```java
public interface UserDao {

  @Select({
      "select id, name from user"
  })
  @ResultMap("userMapper")
  List<User> selectAll();

  @Select("select id, name from user where id = #{id}")
  @ResultMap("userMapper")
  User selectById(@Param("id") int id);

  /**
   * 定义ResultMap 
   */
  @TypeResultMap({
      @Result(property = "id", column = "idFactory"),
      @Result(property = "name", column = "name")
  })
  User userMapper();
}
```

## Mybatis批量插件
mybatis已有的批量更新比较麻烦，要么写动态sql，要么利用BatchExecutor的SqlSession. 
在工程中,更加希望DAO中的方法需要批量的时候用批量,不需要批量的时候不用批量. 有两种方式
实现,一种是实现自定义的Executor,它持有batch与非batch的两个Executor,在执行sql时自
由切换,第二种实现方式则是通过mybatis 插件实现,当需要使用批量时,不使用sqlsession中的
executor,而是使用新的executor. 第一种方式相对稍复杂一点,第二种方式需要将此插件配置
成第一个Executor插件.这里选择使用第二种方式。

此插件用于优化mybatis批量插入/更新/删除的插件. 此插件基于BatchExecutor实现批量更新，
只需要将需要更新的sql id(不包含命名空间)以batch开头，参数需要是Iterable或者数组即可。

### 使用方式
1.配置mybatis插件
```xml
<plugin interceptor="cn.yxffcode.stupidmybatis.core.BatchExecutorInterceptor"></plugin>
```
2.DAO如果需要使用batch则,参数需要是Iterable或者数组,sql的statement id(不包含命名空间)
要以batch开头,如果是映射接口,则方法名以batch开头:
```java
public interface UserDao {

  @Insert({
          "insert into user (id, name) values (#{id}, #{name})"
  }) 
  int batchInsert(List<User> users);
}
```
### 使用建议
此插件的实现原理是拦截Executor的update方法,然后将目标方法的调用改为创建新的BatchExecutor,
然后执行批量的更新, 但新的BatchExecutor对象没有经过InterceptorChain的包装,所以在此插件之前
的Executor拦截器不会被执行,所以最好是将此插件配置在第一个。

## Mybatis in查询参数插件
在使用mybatis做in查询时，需要写动态sql，如果使用xml则要写forEach标签，如果使用注解，则需要写SqlProvider。
此插件为mysql支持参数为Iterable或者数组的情况，自动将in(#{list})转换成in(?, ?, ?)的形式，
不需要再写forEach或者SqlProvider。
### 使用方式
```xml
<plugin interceptor="cn.yxffcode.stupidmybatis.core.ListParameterResolver"></plugin>
```
DAO上的in查询示例：
```java
public interface UserDao {

  @Insert({
          "insert id, name from user where id in (#{userIds})"
  })
  @Results({
    //省略
  }) 
  List<User> selectByIds(@Param("userIds") List<Integer> userIds);
}
```

## 自动分页查询所有数据
mybatis dao自动分页查询。如果在系统启动时需要将各种不同的基础数据全部加载到内存中，可以使用分页查询的方式，
但是会有大量处理分页查询的代码，此插件用于自动执行分页
### 使用方式
1. 配置mybatis插件
```xml
<plugin interceptor="cn.yxffcode.stupidmybatis.core.PageQueryAllInterceptor"></plugin>
```
2. 对于DAO,需要使用此插件的查询方法上加上注解@Paged:
```java
@Select({
        "select id, name from user"
})
@Results({
  //省略
})
@Paged
List<User> selectAll();
```
@Paged注解可以标记在类或者接口的方法上,value属性表示一页的大小,默认为100

3. 创建DAO代理,用于处理分页上下文
```java
UserDao userDao = PagedQueryDaoProxy.wrapNotNull(sqlSession.getMapper(UserDao.class));
```
### 使用Spring
每次使用PagedQueryDaoProxy创建代理比较繁琐，如果是Spring工程，则可以通过Spring自动创建代理。

在Dao上使用任何注解标记,例如@Repository，然后在spring中配置DaoPageQueryAllBeanPostProcessor
```xml
<bean class="cn.yxffcode.mybatispagequeryall.DaoPageQueryAllBeanPostProcessor">
    <constructor-arg value="org.springframework.stereotype.Repository"/>
</bean>
```