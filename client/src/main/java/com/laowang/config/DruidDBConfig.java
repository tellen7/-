package com.laowang.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.laowang.model.GlobalVar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author wangy
 */
@Slf4j
@Configuration
@Component
public class DruidDBConfig {

    @Autowired
    private GlobalVar globalVar;
    /**
     * 声明其为Bean实例
     * 在同样的DataSource中，首先使用被标注的DataSource
     * */
    @Bean
    @Primary
    public DruidDataSource dataSource(){
        DruidDataSource datasource = new DruidDataSource();

        datasource.setUrl(globalVar.getDbUrl());
        datasource.setUsername(globalVar.getUsername());
        datasource.setPassword(globalVar.getPassword());
        datasource.setDriverClassName(globalVar.getDriverClassName());

        //configuration
        datasource.setInitialSize(globalVar.getInitialSize());
        datasource.setMinIdle(globalVar.getMinIdle());
        datasource.setMaxActive(globalVar.getMaxActive());
        datasource.setMaxWait(globalVar.getMaxWait());
        datasource.setTimeBetweenEvictionRunsMillis(globalVar.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(globalVar.getMinEvictableIdleTimeMillis());
        datasource.setValidationQuery(globalVar.getValidationQuery());
        datasource.setTestWhileIdle(globalVar.isTestWhileIdle());
        datasource.setTestOnBorrow(globalVar.isTestOnBorrow());
        datasource.setTestOnReturn(globalVar.isTestOnReturn());
        datasource.setPoolPreparedStatements(globalVar.isPoolPreparedStatements());
        datasource.setMaxPoolPreparedStatementPerConnectionSize(globalVar.getMaxPoolPreparedStatementPerConnectionSize());
        try {
            datasource.setFilters(globalVar.getFilters());
        } catch (SQLException e) {
            log.error("druid configuration initialization filter", e);
        }
        datasource.setConnectionProperties(globalVar.getConnectionProperties());

        return datasource;
    }

    public DruidDataSource init(){
        return this.dataSource();
    }
}
