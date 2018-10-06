package cn.yxffcode.stupidmybatis.data.parser;

import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang
 */
public class TableMetaCache {

  public static TableMetaCache getInstance() {
    return TableMetaCacheHolder.ourInstance;
  }

  private static final class TableMetaCacheHolder {
    private static TableMetaCache ourInstance = new TableMetaCache();
  }

  private TableMetaCache() {
  }

  private Map<Class<?>, ORMConfig> primaryKeyCache = Maps.newHashMap();

  public void parse(ORM orm, ResultMap resultMap, Class<?> mapperInterface) {
    checkNotNull(mapperInterface);
    Class<?> beanType = OrmUtils.getBeanType(mapperInterface);

    BiMap<String, String> mappings = getPropertyColumnMappings(resultMap, (Class<?>) beanType);

    primaryKeyCache.put((Class<?>) beanType, new ORMConfig(orm, mappings));
  }

  private BiMap<String, String> getPropertyColumnMappings(ResultMap resultMap, Class<?> beanType) {
    BiMap<String, String> mappings = HashBiMap.create();
    for (ResultMapping resultMapping : resultMap.getResultMappings()) {
      mappings.put(resultMapping.getProperty(), resultMapping.getColumn());
    }
    return mappings;
  }

  public ORMConfig getORMConfig(Class<?> beanType) {
    return primaryKeyCache.get(beanType);
  }

  public static final class ORMConfig {
    private final ORM orm;
    /**
     * property -> column
     */
    private final BiMap<String, String> mappings;

    public ORMConfig(ORM orm, BiMap<String, String> mappings) {
      this.orm = orm;
      this.mappings = mappings;
    }

    public String getColumn(String property) {
      return mappings.get(property);
    }

    public ORM getOrm() {
      return orm;
    }

    public BiMap<String, String> getMappings() {
      return mappings;
    }
  }
}
