package cn.yxffcode.stupidmybatis.core.execution;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfHandler;
import cn.yxffcode.stupidmybatis.core.cfg.MapperConfigHandler;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.reflection.TypeParameterResolver;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 使用 offset limit的方式分页有缺陷，当数据当很大的时候，这种分页方式有性能问题，
 * where id > #{id} limit #{pageSize}的方式，此注解用于从返回值中抽取max(id)，获取
 * 下一页的时候，将此max(id)作为参数：
 * <p>
 *
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@MapperResultHandler(OrderPagination.Config.class)
@MapperConfHandler(value = OrderPagination.Config.class, order = MapperConfHandler.Order.AFTER_CONFIG_PARSE)
public @interface OrderPagination {

  /**
   * @return 数据的主键或者唯一键等能唯一标识一条数据的DO字段
   */
  String value();

  /**
   * 处理返回结果的类
   */
  final class Config implements MapperResultPostHandler<OrderPagination>, MapperConfigHandler<OrderPagination> {

    @Override
    public void handleAnnotation(OrderPagination orderPagination, Class<?> type, Method method, MapperBuilderAssistant assistant) throws Throwable {
      Class<?> returnType = method.getReturnType();
      if (!(OrderPageList.class.isAssignableFrom(returnType))) {
        return;
      }
      String statementId = type.getName() + '.' + method.getName();
      MappedStatement mappedStatement = assistant.getConfiguration().getMappedStatement(statementId);
      List<ResultMap> resultMaps = mappedStatement.getResultMaps();
      if (resultMaps == null || resultMaps.isEmpty()) {
        return;
      }
      Class<?> statementResultType = getStatementResultType(type, method);
      if (statementResultType == null) {
        return;
      }
      resetResultType(resultMaps, statementResultType);
    }

    private void resetResultType(List<ResultMap> resultMaps, Class<?> statementResultType) {
      for (ResultMap resultMap : resultMaps) {
        Class<?> resultType = resultMap.getType();
        if (OrderPageList.class.isAssignableFrom(resultType)) {
          Reflections.setField(resultMap, "type", statementResultType);
        }
      }
    }

    private Class<?> getStatementResultType(Class<?> type, Method method) {
      Class<?> statementResultType = null;
      Type paramType = TypeParameterResolver.resolveReturnType(method, type);
      if (paramType instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) paramType).getActualTypeArguments();
        statementResultType = (Class<?>) actualTypeArguments[0];
      }
      return statementResultType;
    }

    @Override
    public Object handle(OrderPagination orderPagination, Class<?> type, Method method, Object proxy, Object result) throws Throwable {
      if (result == null) {
        return OrderPageList.emptyList();
      }
      OrderPageList<Object, Object> list = (OrderPageList<Object, Object>) result;
      if (list.isEmpty()) {
        return OrderPageList.emptyList();
      }
      //取出最后一个元素的id
      Object last = list.get(list.size() - 1);
      list.setLastId(Reflections.getField(orderPagination.value(), last));
      return list;
    }
  }
}