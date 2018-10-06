package cn.yxffcode.stupidmybatis.data.cfg;

import org.apache.ibatis.annotations.DeleteProvider;

import java.lang.annotation.Annotation;

/**
 * @author gaohang
 */
public class SpecifiedDeleteProvider extends AbstractStatementProvider implements DeleteProvider {

  public SpecifiedDeleteProvider(Class<?> providerType, String providerMethod) {
    super(providerType, providerMethod);
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return DeleteProvider.class;
  }

}
