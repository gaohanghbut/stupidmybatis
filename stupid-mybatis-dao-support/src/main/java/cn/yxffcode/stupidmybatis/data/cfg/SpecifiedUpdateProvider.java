package cn.yxffcode.stupidmybatis.data.cfg;

import org.apache.ibatis.annotations.UpdateProvider;

import java.lang.annotation.Annotation;

/**
 * @author gaohang
 */
public class SpecifiedUpdateProvider extends AbstractStatementProvider implements UpdateProvider {

  public SpecifiedUpdateProvider(Class<?> providerType, String providerMethod) {
    super(providerType, providerMethod);
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return UpdateProvider.class;
  }
}
