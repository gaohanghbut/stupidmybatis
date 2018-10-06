package cn.yxffcode.stupidmybatis.data.cfg;

import org.apache.ibatis.annotations.SelectProvider;

import java.lang.annotation.Annotation;

/**
 * @author gaohang
 */
public class SpecifiedSelectProvider extends AbstractStatementProvider implements SelectProvider {

  public SpecifiedSelectProvider(Class<?> providerType, String providerMethod) {
    super(providerType, providerMethod);
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return SelectProvider.class;
  }
}
