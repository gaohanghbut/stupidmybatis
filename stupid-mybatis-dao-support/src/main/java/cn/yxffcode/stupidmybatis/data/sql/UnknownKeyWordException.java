package cn.yxffcode.stupidmybatis.data.sql;

/**
 * @author gaohang
 */
public class UnknownKeyWordException extends RuntimeException {
  public UnknownKeyWordException() {
  }

  public UnknownKeyWordException(String message) {
    super(message);
  }

  public UnknownKeyWordException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnknownKeyWordException(Throwable cause) {
    super(cause);
  }

  public UnknownKeyWordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
