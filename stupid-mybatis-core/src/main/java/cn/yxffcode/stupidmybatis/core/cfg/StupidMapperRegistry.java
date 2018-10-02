package cn.yxffcode.stupidmybatis.core.cfg;

import cn.yxffcode.stupidmybatis.core.execution.StupidMapperProxy;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.util.HashSet;
import java.util.Set;

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
  public boolean hasMapper(Class<?> type) {
    return knownMappers.contains(type);
  }

  @Override
  public void addMapper(Class<?> type) {
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

}
