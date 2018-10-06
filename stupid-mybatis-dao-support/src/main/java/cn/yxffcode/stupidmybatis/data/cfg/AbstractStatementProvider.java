package cn.yxffcode.stupidmybatis.data.cfg;

import java.util.Objects;

/**
 * @author gaohang
 */
abstract class AbstractStatementProvider {
  protected final Class<?> providerType;
  protected final String providerMethod;

  public AbstractStatementProvider(Class<?> providerType, String providerMethod) {
    this.providerType = providerType;
    this.providerMethod = providerMethod;
  }

  public Class<?> type() {
    return providerType;
  }

  public String method() {
    return providerMethod;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractStatementProvider that = (AbstractStatementProvider) o;
    return Objects.equals(providerType, that.providerType) &&
        Objects.equals(providerMethod, that.providerMethod);
  }

  @Override
  public int hashCode() {

    return Objects.hash(providerType, providerMethod);
  }

  @Override
  public String toString() {
    return "{" +
        "providerType=" + providerType +
        ", providerMethod='" + providerMethod + '\'' +
        '}';
  }
}
