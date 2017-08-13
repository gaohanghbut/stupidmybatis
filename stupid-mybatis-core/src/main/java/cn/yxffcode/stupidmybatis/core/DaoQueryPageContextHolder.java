package cn.yxffcode.stupidmybatis.core;

/**
 * @author gaohang on 16/8/5.
 */
public abstract class DaoQueryPageContextHolder {
  private static final ThreadLocal<Paged> pageContext = new ThreadLocal<>();

  private DaoQueryPageContextHolder() {
  }

  public static Paged get() {
    return pageContext.get();
  }

  public static void remove() {
    pageContext.remove();
  }

  public static void set(Paged paged) {
    pageContext.set(paged);
  }
}
