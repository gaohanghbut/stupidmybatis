package cn.yxffcode.stupidmybatis.core.cfg;

import cn.yxffcode.stupidmybatis.core.execution.StupidMapperProxy;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

/**
 * @author gaohang
 */
public class StupidMapperRegistry extends MapperRegistry {
  private Set<Class<?>> knownMappers = new HashSet<Class<?>>();
  private Configuration config;

  public StupidMapperRegistry(Configuration config) {
    super(config);
    this.config = config;
  }

  @Override
  public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    if (!knownMappers.contains(type))
      throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
    try {
      return StupidMapperProxy.newMapperProxy(type, sqlSession);
    } catch (Exception e) {
      throw new BindingException("Error getting mapper instance. Cause: " + e, e);
    }
  }

  @Override
  public <T> boolean hasMapper(Class<T> type) {
    return this.knownMappers.contains(type);
  }


  @Override
  public <T> void addMapper(Class<T> type) {
    if (type.isInterface()) {
      if (knownMappers.contains(type)) {
        throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
      }
      boolean loadCompleted = false;
      try {
        knownMappers.add(type);
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser.  If the type is already known, it won't try.
        MapperAnnotationBuilder parser = new StupidAnnotationParser(config, type);
        parser.parse();
        loadCompleted = true;
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type);
        }
      }
    }
  }

  @Override
  public Collection<Class<?>> getMappers() {
    return Collections.unmodifiableCollection(knownMappers);
  }

  @Override
  public void addMappers(String packageName, Class<?> superType) {
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil();
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
    Iterator iterator = mapperSet.iterator();

    while (iterator.hasNext()) {
      Class<?> mapperClass = (Class) iterator.next();
      this.addMapper(mapperClass);
    }

  }

  @Override
  public void addMappers(String packageName) {
    this.addMappers(packageName, Object.class);
  }
}
