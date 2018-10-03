# StupidMybatis
StupidMybatis是一个mybatis扩展框架，用于简化使用mybatis的过程中的一些痛点，详细见下文。

使用StupidMybatis，先将spring中的SqlSessionFactoryBean替换成StupidSqlSessionFactoryBean,使用方式与SqlSessionFactory相同，例如：
```xml
  <bean id = "sqlSession" class="cn.yxffcode.stupidmybatis.spring.StupidSqlSessionFactoryBean">
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

原始的通过@Result配置的代码如下：
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
   * 定义ResultMap， 可指定id和resultType作为resultMapId和resultMap的返回类型, 
   * 默认的resultMapId为方法名，默认的返回类型是方法的返回类型或者方法的返回类型中的元素
   */
  @TypeResultMap({
      @Result(property = "id", column = "idFactory"),
      @Result(property = "name", column = "name")
  })
  User userMapper();
}
```
TypeResultMap还可声明在Mapper接口的类型上，但必须要指定id和resultType，无法使用这两个属性的默认值：
```java
/**
 * 定义ResultMap， 必须指定id和resultType作为resultMapId和resultMap的返回类型
 */
@TypeResultMap(id = "userMapper", resultType = User.class, value = {
  @Result(property = "id", column = "idFactory"),
  @Result(property = "name", column = "name")
})
public interface UserDao {

  @Select({
      "select id, name from user"
  })
  @ResultMap("userMapper")
  List<User> selectAll();

  @Select("select id, name from user where id = #{id}")
  @ResultMap("userMapper")
  User selectById(@Param("id") int id);
}
```

## 指定默认方法作为ResultMap
@Results/@TypeResultMap只是简单的映射，没有任务的转换逻辑，假如转换成POJO的时候，需要有转换逻辑（例如多个字段拼接）
可以指定一个默认方法作为ResultMap，使用方式如下：

```java
public interface UserDao {

  @Select("select id, name from user where id = #{id}")
  @MapperMethod("mapToUser")//指定mapToUser方法作为ResultMap
  User selectById(@Param("id") int id);

  /**
   * 此方法作为ResultMap，这种用法需要Java8以上
   */
  default User mapToUser(Map<String, ?> result) {
    if (result == null) {
      return null;
    }
    User user = new User();
    //mybatis返回的map中，字段都是大写
    user.setId((Integer) result.get("ID"));
    user.setName((String) result.get("NAME"));
    return user;
  }
}
```
映射方法的参数需要是Map

指定默认方法作为ResultMap这种使用方式，还可以结合其它ResultMap，例如：
```java
public interface UserDao {

  @Select("select id, name from user where id = #{id}")
  @ResultMap("mapMapper")//指定一个名为mapMapper的返回Map的ResultMap
  @MapperMethod("mapToUser")//转换mapMapper返回的Map
  User selectById(@Param("id") int id);
  
  /**
   *  mapMapper is a result map
   */
  @TypeResultMap({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  Map mapMapper();

  /**
   * @param result
   * @return
   */
  default User mapToUser(Map<String, ?> result) {
    if (result == null) {
      return null;
    }
    User user = new User();
    user.setId((Integer) result.get("id"));
    user.setName((String) result.get("name"));
    return user;
  }

}

```

上面的例子中，先使用mapMapper将结果转换成Map，再通过mapToUser方法将Map转换成User。

同时使用ResultMap和映射方法的时候，映射方法的参数需要和ResultMap的返回类型相同，
假如指定的ResultMap返回的是一个POJO对象，比如User，则映射方法的参数需要是User，例如：
```java
public interface UserDao {

  @Select("select id, name from user where id = #{id}")
  @ResultMap("userMapper")
  @MapperMethod("userTransform")
  User selectById(@Param("id") int id);

  @TypeResultMap({
      @Result(property = "id", column = "id"),
      @Result(property = "name", column = "name")
  })
  User userMapper();

  default User userTransform(User user) {
    if (user == null) {
      return null;
    }
    user.setName("hello " + user.getName());//修改name的值
    return user;
  }

}

```

## 调用映射接口的默认方法
原生mybatis不支持java8的默认方法调用，会有找不到statement的异常，stupidmybatis支持默认方法的调用，例如如下DAO中，mapToUser方法可单独调用

```java
public interface UserDao {

  @Select("select id, name from user where id = #{id}")
  @MapperMethod("userTransform")
  User selectById(@Param("id") int id);

  default User mapToUser(Map<String, ?> result) {
    if (result == null) {
      return null;
    }
    User user = new User();
    user.setId((Integer) result.get("id"));
    user.setName((String) result.get("name"));
    return user;
  }

}

```
调用默认方法：
```java
Map<String, Object> map = Maps.newHashMap();
map.put("id", 0);
map.put("name", "hello");
User user = userDao.mapToUser(map);
```
## 注解扩展
### 对返回结果进行特定的加工
除了上面@MapperMethod能调用默认方法处理返回结果外，还可通过自定义注解的方式处理返回结果，
自定义注解需要通过@MapperResultHandler元注解提供MapperResultPostHandler的实现类，
例如实现一个@PostProcessResult用于打印日志：
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperResultHandler(PostProcessResult.PostProcesser.class)//这里指定MapperResultPostHandler
public @interface PostProcessResult {

  /**
   * 处理返回结果的类
   */
  final class PostProcesser implements MapperResultPostHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostProcessResult.class);

    @Override
    public Object handle(Class<?> type, Method method, Object proxy, Object result) throws Throwable {
      LOGGER.info("{}.{} result is {}", type.getName(), method.getName(), result);
      return result;
    }
  }
}
```
可在DAO上使用@PostProcessResult
```java
@TypeResultMap(id = "userMapper", resultType = User.class, value = {
    @Result(property = "id", column = "id"),
    @Result(property = "name", column = "name")
})
public interface UserDao {

  @Select("select id, name from user where id = #{id}")
  @ResultMap("userMapper")
  @PostProcessResult//此处可使用自定义的结果处理注解
  User selectById(@Param("id") int id);

}

```
### 通过注解对配置的扩展
可自定义注解用于对mybatis进行配置，注解中需要通过@MapperConfHandler元注解指定
MapperConfigHandler接口的实现，例如实现一个自动在sql后面拼上limit的注解@Limit：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperConfHandler(value = Limit.Config.class, order = MapperConfHandler.Order.AFTER_CONFIG_PARSE)
public @interface Limit {

  /**
   * @return limit 的大小
   */
  int value();

  /**
   * 处理返回结果的类
   */
  final class Config implements MapperConfigHandler<Limit> {
    @Override
    public void handleAnnotation(Limit limit, Class<?> type, Method method, MapperBuilderAssistant assistant) throws Throwable {
      //通过assistant注册配置，不清楚可看看mybatis源码
      String statementId = type.getName() + '.' + method.getName();
      MappedStatement mappedStatement = assistant.getConfiguration().getMappedStatement(statementId);
      
      Reflections.setField(mappedStatement, "sqlSource", new SqlSource() {
        private final SqlSource delegate = mappedStatement.getSqlSource();
        private final Configuration configuration = assistant.getConfiguration();

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
          BoundSql boundSql = delegate.getBoundSql(parameterObject);
          return new BoundSql(configuration, boundSql.getSql() + "limit " + limit.value(), boundSql.getParameterMappings(), boundSql.getParameterObject());
        }
      });
    }

  }
}
```
在DAO上使用@Limit
```java
@TypeResultMap(id = "userMapper", resultType = User.class, value = {
    @Result(property = "id", column = "id"),
    @Result(property = "name", column = "name")
})
public interface UserDao {

  @Select("select id, name from user where id >= #{minId}")
  @ResultMap("userMapper")
  @Limit(100) //limit 100
  List<User> selectUsers(@Param("minId") int id);

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