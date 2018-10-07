package cn.yxffcode.stupidmybatis.data.sql;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.data.parser.TableMetaCache;
import cn.yxffcode.stupidmybatis.data.utils.OrmUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gaohang
 */
public abstract class KeyWordHandler {

  private static final KeyWordHandler DEFAULT_KEYWORD_HANDLER = new DefaultKeyWordHandler();

  protected final SqlContentProvider commonKeyWordSqlContextProvider = new CommonKeyWordContextProvider();
  protected final Table<Class<?>, String, SqlContentProvider> sqlContextProviders = HashBasedTable.create();
  protected final Table<Class<?>, String, KeyWord> keywordAnnotations = HashBasedTable.create();

  public static KeyWordHandler getInstance() {
    return DEFAULT_KEYWORD_HANDLER;
  }

  public abstract void handleKeyWords(Class<?> mapperInterface, Method method, TableMetaCache.ORMConfig ormConfig, MapperBuilderAssistant assistant) throws Throwable;

  public void registKeyWord(Class<?> mapperInterface, KeyWord keyWord) {
    try {
      sqlContextProviders.put(mapperInterface, keyWord.name(), keyWord.contentProvider().newInstance());
      keywordAnnotations.put(mapperInterface, keyWord.name(), keyWord);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  protected TableMetaCache.ORMConfig getOrmConfig(Class<?> mapperInterface) {
    Class<?> ormEntityClass = OrmUtils.getOrmEntityClass(mapperInterface);
    return OrmUtils.getOrmConfig(ormEntityClass);
  }

  private static final class DefaultKeyWordHandler extends KeyWordHandler {

    private static final Pattern KEYWORD_PATTERN = Pattern.compile("@\\w+");

    @Override
    public void handleKeyWords(Class<?> mapperInterface, Method method, TableMetaCache.ORMConfig ormConfig, MapperBuilderAssistant assistant) throws Throwable {

      //通过assistant注册配置，不清楚可看看mybatis源码
      String statementId = assistant.getCurrentNamespace() + '.' + method.getName();
      MappedStatement mappedStatement = assistant.getConfiguration().getMappedStatement(statementId);

      Reflections.setField(mappedStatement, "sqlSource", new SqlSource() {
        private final SqlSource delegate = mappedStatement.getSqlSource();
        private final Configuration configuration = assistant.getConfiguration();

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
          BoundSql boundSql = delegate.getBoundSql(parameterObject);
          String sql = boundSql.getSql();
          if (!sql.contains("@")) {
            return boundSql;
          }
          //parse parameter
          StringBuilder newSql = new StringBuilder();
          int lastEnd = 0;
          Matcher matcher = KEYWORD_PATTERN.matcher(sql);
          while (matcher.find()) {
            int start = matcher.start();
            newSql.append(sql, lastEnd, start);
            String keyWordRef = matcher.group();
            lastEnd = start + keyWordRef.length();
            String keyword = keyWordRef.substring(1);
            SqlContentProvider sqlContentProvider = sqlContextProviders.get(mapperInterface, keyword);
            KeyWord keyWordAnnotation = keywordAnnotations.get(mapperInterface, keyword);
            if (sqlContentProvider != null) {
              newSql.append(sqlContentProvider.getContent(keyWordAnnotation, ormConfig, mappedStatement));
            } else {
              KeyWord keyWord = CommonKeyWordContextProvider.getKeyWord(keyword);
              if (keyWord == null) {
                throw new UnknownKeyWordException(keyword + " is not found in mapper " + mapperInterface.getName());
              }
              newSql.append(commonKeyWordSqlContextProvider.getContent(keyWord, ormConfig, mappedStatement));
            }
          }
          //append end
          if (lastEnd < sql.length()) {
            newSql.append(sql, lastEnd, sql.length());
          }
          return new BoundSql(configuration, newSql.toString(), boundSql.getParameterMappings(), boundSql.getParameterObject());
        }
      });
    }

  }

  private static final class CommonKeyWordContextProvider implements SqlContentProvider {

    private static final String PROPERTIES_KEYWORD = "properties";
    private static final String COLUMNS_KEYWORD = "columns";
    private static final String PRIMARYKEY_KEYWORD = "primaryKey";

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    private final static Map<String, KeyWord> COMMON_KEYWORDS = Maps.newHashMap();

    static {
      COMMON_KEYWORDS.put(PROPERTIES_KEYWORD, new CommonKeyWord(PROPERTIES_KEYWORD));
      COMMON_KEYWORDS.put(COLUMNS_KEYWORD, new CommonKeyWord(COLUMNS_KEYWORD));
      COMMON_KEYWORDS.put(PRIMARYKEY_KEYWORD, new CommonKeyWord(PRIMARYKEY_KEYWORD));
    }

    private static final class CommonKeyWord implements KeyWord {
      private final String name;

      private CommonKeyWord(String name) {
        this.name = name;
      }

      @Override
      public String name() {
        return name;
      }

      @Override
      public String value() {
        throw new UnsupportedOperationException("cannot invoke value() method in CommonKeyWord");
      }

      @Override
      public Class<? extends SqlContentProvider> contentProvider() {
        throw new UnsupportedOperationException("cannot invoke contentProvider() method in CommonKeyWord");
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return KeyWord.class;
      }
    }

    private static KeyWord getKeyWord(String keyword) {
      return COMMON_KEYWORDS.get(keyword);
    }

    @Override
    public String getContent(KeyWord keyWord, TableMetaCache.ORMConfig ormConfig, MappedStatement mappedStatement) {
      switch (keyWord.name()) {
        case PROPERTIES_KEYWORD: {
          Set<String> properties = ormConfig.getProperties();
          return COMMA_JOINER.join(properties);
        }
        case COLUMNS_KEYWORD: {
          Set<String> columns = ormConfig.getColumns();
          return COMMA_JOINER.join(columns);
        }
        case PRIMARYKEY_KEYWORD: {
          String[] keyColumns = ormConfig.getOrm().primaryKey().keyColumns();
          return COMMA_JOINER.join(keyColumns);
        }
        default:
          throw new UnknownKeyWordException(keyWord.name() + " is unknown, plz define it");
      }
    }
  }

}
