package cn.yxffcode.stupidmybatis.data.cfg;

import org.apache.ibatis.annotations.ResultMap;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * @author gaohang
 */
public class SpecifiedResultMap implements ResultMap {

  private final String[] resultMapName;

  public SpecifiedResultMap(String resultMapName) {
    this.resultMapName = new String[]{resultMapName};
  }

  @Override
  public String[] value() {
    return resultMapName;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return ResultMap.class;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SpecifiedResultMap that = (SpecifiedResultMap) o;
    return Arrays.equals(resultMapName, that.resultMapName);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(resultMapName);
  }

  @Override
  public String toString() {
    return "SpecifiedResultMap{" +
        "resultMapName=" + Arrays.toString(resultMapName) +
        '}';
  }
}
