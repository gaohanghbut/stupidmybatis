package cn.yxffcode.stupidmybatis.commons;

import com.google.common.base.Throwables;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author gaohang on 15/12/4.
 */
public final class Reflections {
  private Reflections() {
  }

  private static Field findField(Class<?> clazz, String name) {
    return findField(clazz, name, null);
  }

  public static Field findField(Class<?> clazz, String name, Class<?> type) {
    Class<?> searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Field[] fields = searchType.getDeclaredFields();
      for (Field field : fields) {
        if ((name == null || name.equals(field.getName())) && (type == null || type
            .equals(field.getType()))) {
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  public static Object getField(String fieldName, Object target) {
    Field field = findField(target.getClass(), fieldName);
    if (!field.isAccessible()) {
      field.setAccessible(true);
    }
    try {
      return field.get(target);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass()
          .getName() + ": " + ex.getMessage(), ex);
    }
  }

  public static void setField(Object target, String fieldName, Object value) {
    try {
      final Field field = target.getClass().getDeclaredField(fieldName);
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      field.set(target, value);
    } catch (Exception ex) {
      throw new IllegalStateException("Unexpected reflection exception - " + ex.getClass()
          .getName() + ": " + ex.getMessage(), ex);
    }
  }

  public static <T> T call(Annotation annotation, String methodName) {
    try {
      Method method = annotation.annotationType().getMethod(methodName);
      return (T) method.invoke(annotation);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
