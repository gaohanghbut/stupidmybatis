package cn.yxffcode.stupidmybatis.springboot;

import cn.yxffcode.stupidmybatis.core.BatchExecutorInterceptor;
import cn.yxffcode.stupidmybatis.core.ListParameterResolver;
import cn.yxffcode.stupidmybatis.core.PageQueryAllInterceptor;
import cn.yxffcode.stupidmybatis.spring.DaoPageQueryAllBeanPostProcessor;
import cn.yxffcode.stupidmybatis.spring.StupidSqlSessionFactoryBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.annotation.*;
import java.util.List;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(EnableStupidMybatis.StupidMybatisImportSelector.class)
public @interface EnableStupidMybatis {

  final class StupidMybatisImportSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(org.springframework.core.type.AnnotationMetadata annotationMetadata) {
      return new String[]{StupidMybatisAutoConfig.class.getName()};
    }
  }

  @Configuration
  class StupidMybatisPluginAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public PageQueryAllInterceptor pageQueryAllInterceptor() {
      return new PageQueryAllInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public DaoPageQueryAllBeanPostProcessor daoPageQueryAllBeanPostProcessor() {
      return new DaoPageQueryAllBeanPostProcessor(Mapper.class.getName());
    }

    @Bean
    @ConditionalOnMissingBean
    public ListParameterResolver listParameterResolver() {
      return new ListParameterResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchExecutorInterceptor batchExecutorInterceptor() {
      return new BatchExecutorInterceptor();
    }

  }

  @Configuration
  @AutoConfigureBefore(MybatisAutoConfiguration.class)
  @AutoConfigureAfter(StupidMybatisPluginAutoConfig.class)
  @EnableConfigurationProperties(MybatisProperties.class)
  class StupidMybatisAutoConfig {
    private final MybatisProperties properties;
    private final Interceptor[] interceptors;
    private final ResourceLoader resourceLoader;
    private final DatabaseIdProvider databaseIdProvider;
    private final List<ConfigurationCustomizer> configurationCustomizers;

    public StupidMybatisAutoConfig(MybatisProperties properties,
                                   ObjectProvider<Interceptor[]> interceptorsProvider,
                                   ResourceLoader resourceLoader,
                                   ObjectProvider<DatabaseIdProvider> databaseIdProvider,
                                   ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizersProvider) {
      this.properties = properties;
      this.interceptors = interceptorsProvider.getIfAvailable();
      this.resourceLoader = resourceLoader;
      this.databaseIdProvider = databaseIdProvider.getIfAvailable();
      this.configurationCustomizers = configurationCustomizersProvider.getIfAvailable();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
      StupidSqlSessionFactoryBean factory = new StupidSqlSessionFactoryBean();
      factory.setDataSource(dataSource);
      factory.setVfs(SpringBootVFS.class);
      if (StringUtils.hasText(this.properties.getConfigLocation())) {
        factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
      }
      org.apache.ibatis.session.Configuration configuration = this.properties.getConfiguration();
      if (configuration == null && !StringUtils.hasText(this.properties.getConfigLocation())) {
        configuration = new org.apache.ibatis.session.Configuration();
      }
      if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
        for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
          customizer.customize(configuration);
        }
      }
      factory.setConfiguration(configuration);
      if (this.properties.getConfigurationProperties() != null) {
        factory.setConfigurationProperties(this.properties.getConfigurationProperties());
      }
      if (!ObjectUtils.isEmpty(this.interceptors)) {
        factory.setPlugins(this.interceptors);
      }
      if (this.databaseIdProvider != null) {
        factory.setDatabaseIdProvider(this.databaseIdProvider);
      }
      if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
        factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
      }
      if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
        factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
      }
      if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
        factory.setMapperLocations(this.properties.resolveMapperLocations());
      }

      return factory.getObject();
    }

  }
}
