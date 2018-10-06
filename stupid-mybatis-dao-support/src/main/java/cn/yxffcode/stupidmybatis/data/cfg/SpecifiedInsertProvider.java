package cn.yxffcode.stupidmybatis.data.cfg;

import org.apache.ibatis.annotations.InsertProvider;

import java.lang.annotation.Annotation;

/**
 * @author gaohang
 */
public class SpecifiedInsertProvider extends AbstractStatementProvider implements InsertProvider {
  public SpecifiedInsertProvider(Class<?> providerType, String providerMethod) {
    super(providerType, providerMethod);
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return InsertProvider.class;
  }
}
