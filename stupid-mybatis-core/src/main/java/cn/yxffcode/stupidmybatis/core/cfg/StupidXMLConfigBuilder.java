package cn.yxffcode.stupidmybatis.core.cfg;

import cn.yxffcode.stupidmybatis.commons.Reflections;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * @author gaohang
 */
public class StupidXMLConfigBuilder extends XMLConfigBuilder {

  public StupidXMLConfigBuilder(Reader reader, String environment, Properties props) {
    super(reader, environment, props);
  }
  public StupidXMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
    super(inputStream, environment, props);
    Reflections.setField(this.configuration, "mapperRegistry", new StupidMapperRegistry(this.configuration));
  }
}
