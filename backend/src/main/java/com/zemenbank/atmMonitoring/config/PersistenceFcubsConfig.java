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

import javax.sql.DataSource;
import java.util.Objects;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.zemenbank.atmMonitoring.repositories.bankworld",
        entityManagerFactoryRef = "fcubsEntityManager",
        transactionManagerRef = "fcubsTransactionManager"
)
public class PersistenceFcubsConfig {
        private final Environment env;

    PersistenceFcubsConfig(Environment env){
        super();
        this.env = env;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean fcubsEntityManager() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(fcubsDataSource());
        em.setPackagesToScan("com.zemenbank.atmMonitoring.model.bankworld");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Bean
    public DataSource fcubsDataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(Objects.requireNonNull(env.getProperty("fcubs.datasource.driver-class-name")));
        dataSource.setUrl(Objects.requireNonNull(env.getProperty("fcubs.datasource.url")));
        dataSource.setUsername(Objects.requireNonNull(env.getProperty("fcubs.datasource.username")));
        dataSource.setPassword(Objects.requireNonNull(env.getProperty("fcubs.datasource.password")));

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager fcubsTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(fcubsEntityManager().getObject());
        return transactionManager;
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", env.getProperty("fcubs.jpa.database-platform"));
        properties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("fcubs.jpa.hibernate.ddl-auto"));
        properties.setProperty("hibernate.use-new-id-generator-mappings", env.getProperty("fcubs.jpa.hibernate.use-new-id-generator-mappings"));
        return properties;
    }
}
