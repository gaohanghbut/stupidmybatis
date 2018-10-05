package cn.yxffcode.stupidmybatis.data.utils;

import cn.yxffcode.stupidmybatis.data.BaseDataAccess;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author gaohang
 */
public abstract class OrmUtils {
  private OrmUtils() {
  }
  public static Class<?> getBeanType(Class<?> mapperInterface) {
    Type[] genericInterfaces = mapperInterface.getGenericInterfaces();

    Class<?> beanType = null;
    for (Type genericInterface : genericInterfaces) {
      if (genericInterface instanceof ParameterizedType) {

        //取出类型参数
        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
        if (parameterizedType.getRawType() != BaseDataAccess.class) {
          continue;
        }
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        Type actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
          beanType = (Class<?>) actualTypeArgument;
          break;
        }
      }

    }
    return beanType;
  }

}
