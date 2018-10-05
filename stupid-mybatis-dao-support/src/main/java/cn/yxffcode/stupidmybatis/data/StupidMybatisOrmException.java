package cn.yxffcode.stupidmybatis.data;

/**
 * @author gaohang
 */
public class StupidMybatisOrmException extends RuntimeException {
  public StupidMybatisOrmException() {
  }

  public StupidMybatisOrmException(String message) {
    super(message);
  }

  public StupidMybatisOrmException(String message, Throwable cause) {
    super(message, cause);
  }

  public StupidMybatisOrmException(Throwable cause) {
    super(cause);
  }

  public StupidMybatisOrmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
