package cn.yxffcode.stupidmybatis.core;

import com.google.common.collect.Lists;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author gaohang
 */
final class ResultMapBuilder {
  private final MapperBuilderAssistant assistant;
  private final Class<?> type;
  private final Configuration configuration;

  ResultMapBuilder(MapperBuilderAssistant assistant, Class<?> type) {
    this.assistant = assistant;
    this.type = type;
    this.configuration = assistant.getConfiguration();
  }


  public List<ResultMapping> applyResults(Result[] results, Class<?> resultType) {
    final List<ResultMapping> resultMappings = Lists.newArrayListWithCapacity(results.length);
    for (Result result : results) {
      List<ResultFlag> flags = new ArrayList<>();
      if (result.id()) {
        flags.add(ResultFlag.ID);
      }
      @SuppressWarnings("unchecked")
      Class<? extends TypeHandler<?>> typeHandler = (Class<? extends TypeHandler<?>>)
          ((result.typeHandler() == UnknownTypeHandler.class) ? null : result.typeHandler());
      ResultMapping resultMapping = assistant.buildResultMapping(
          resultType,
          nullToEmpty(result.property()),
          nullToEmpty(result.column()),
          result.javaType() == void.class ? null : result.javaType(),
          result.jdbcType() == JdbcType.UNDEFINED ? null : result.jdbcType(),
          hasNestedSelect(result) ? nestedSelectId(result) : null,
          null,
          null,
          null,
          typeHandler,
          flags,
          null,
          null,
          isLazy(result));
      resultMappings.add(resultMapping);
    }
    return resultMappings;
  }

  private boolean hasNestedSelect(Result result) {
    if (result.one().select().length() > 0 && result.many().select().length() > 0) {
      throw new BuilderException("Cannot use both @One and @Many annotations in the same @Result");
    }
    return result.one().select().length() > 0 || result.many().select().length() > 0;
  }

  private String nestedSelectId(Result result) {
    String nestedSelect = result.one().select();
    if (nestedSelect.length() < 1) {
      nestedSelect = result.many().select();
    }
    if (!nestedSelect.contains(".")) {
      nestedSelect = type.getName() + "." + nestedSelect;
    }
    return nestedSelect;
  }

  private boolean isLazy(Result result) {
    boolean isLazy = configuration.isLazyLoadingEnabled();
    if (result.one().select().length() > 0 && FetchType.DEFAULT != result.one().fetchType()) {
      isLazy = result.one().fetchType() == FetchType.LAZY;
    } else if (result.many().select().length() > 0 && FetchType.DEFAULT != result.many().fetchType()) {
      isLazy = result.many().fetchType() == FetchType.LAZY;
    }
    return isLazy;
  }

}
