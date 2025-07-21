package com.zemenbank.atmMonitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;


@Configuration
@EnableJpaRepositories(
        basePackages = "com.zemenbank.atmMonitoring.repositories.atmMonitor",
        entityManagerFactoryRef = "localEntityManager",
        transactionManagerRef = "localTransactionManager"
)
public class PersistenceLocalConfig {
    private final Environment env;

    PersistenceLocalConfig(Environment env){
        super();
        this.env = env;
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean localEntityManager() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(localDataSource());
        em.setPackagesToScan("com.zemenbank.atmMonitoring.model.atmMonitor");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Primary
    @Bean
    public DataSource localDataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(Objects.requireNonNull(env.getProperty("local.datasource.url")));
        dataSource.setUsername(Objects.requireNonNull(env.getProperty("local.datasource.username")));
        dataSource.setPassword(Objects.requireNonNull(env.getProperty("local.datasource.password")));
        return dataSource;
    }

    @Primary
    @Bean
    public PlatformTransactionManager localTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(localEntityManager().getObject());
        return transactionManager;
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", env.getProperty("local.jpa.properties.hibernate.dialect"));
        properties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("local.jpa.hibernate.ddl-auto"));
        properties.setProperty("hibernate.show-sql", env.getProperty("local.jpa.show-sql"));
        return properties;
    }
}
