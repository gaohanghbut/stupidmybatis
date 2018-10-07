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
public abstract class MacroHandler {

  private static final MacroHandler DEFAULT_KEYWORD_HANDLER = new DefaultMacroHandler();

  protected final SqlContentProvider commonKeyWordSqlContextProvider = new CommonKeyWordContextProvider();
  protected final Table<Class<?>, String, SqlContentProvider> sqlContextProviders = HashBasedTable.create();
  protected final Table<Class<?>, String, Macro> keywordAnnotations = HashBasedTable.create();

  public static MacroHandler getInstance() {
    return DEFAULT_KEYWORD_HANDLER;
  }

  public abstract void handleKeyWords(Class<?> mapperInterface, Method method, TableMetaCache.ORMConfig ormConfig, MapperBuilderAssistant assistant) throws Throwable;

  public void registKeyWord(Class<?> mapperInterface, Macro macro) {
    try {
      sqlContextProviders.put(mapperInterface, macro.name(), macro.contentProvider().newInstance());
      keywordAnnotations.put(mapperInterface, macro.name(), macro);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  protected TableMetaCache.ORMConfig getOrmConfig(Class<?> mapperInterface) {
    Class<?> ormEntityClass = OrmUtils.getOrmEntityClass(mapperInterface);
    return OrmUtils.getOrmConfig(ormEntityClass);
  }

  private static final class DefaultMacroHandler extends MacroHandler {

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
            Macro macroAnnotation = keywordAnnotations.get(mapperInterface, keyword);
            if (sqlContentProvider != null) {
              newSql.append(sqlContentProvider.getContent(macroAnnotation, ormConfig, mappedStatement));
            } else {
              Macro macro = CommonKeyWordContextProvider.getKeyWord(keyword);
              if (macro == null) {
                throw new UnknownKeyWordException(keyword + " is not found in mapper " + mapperInterface.getName());
              }
              newSql.append(commonKeyWordSqlContextProvider.getContent(macro, ormConfig, mappedStatement));
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

    private final static Map<String, Macro> COMMON_KEYWORDS = Maps.newHashMap();

    static {
      COMMON_KEYWORDS.put(PROPERTIES_KEYWORD, new CommonMacro(PROPERTIES_KEYWORD));
      COMMON_KEYWORDS.put(COLUMNS_KEYWORD, new CommonMacro(COLUMNS_KEYWORD));
      COMMON_KEYWORDS.put(PRIMARYKEY_KEYWORD, new CommonMacro(PRIMARYKEY_KEYWORD));
    }

    private static final class CommonMacro implements Macro {
      private final String name;

      private CommonMacro(String name) {
        this.name = name;
      }

      @Override
      public String name() {
        return name;
      }

      @Override
      public String value() {
        throw new UnsupportedOperationException("cannot invoke value() method in CommonMacro");
      }

      @Override
      public Class<? extends SqlContentProvider> contentProvider() {
        throw new UnsupportedOperationException("cannot invoke contentProvider() method in CommonMacro");
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return Macro.class;
      }
    }

    private static Macro getKeyWord(String keyword) {
      return COMMON_KEYWORDS.get(keyword);
    }

    @Override
    public String getContent(Macro macro, TableMetaCache.ORMConfig ormConfig, MappedStatement mappedStatement) {
      switch (macro.name()) {
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
          throw new UnknownKeyWordException(macro.name() + " is unknown, plz define it");
      }
    }
  }

}
