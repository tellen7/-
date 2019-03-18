package com.laowang.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;


/**
 * @author wangy
 */
@Data
@Component
public class GlobalVar {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.initialSize}")
    private int initialSize;

    @Value("${spring.datasource.minIdle}")
    private int minIdle;

    @Value("${spring.datasource.maxActive}")
    private int maxActive;

    @Value("${spring.datasource.maxWait}")
    private int maxWait;

    @Value("${spring.datasource.timeBetweenEvictionRunsMillis}")
    private int timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.minEvictableIdleTimeMillis}")
    private int minEvictableIdleTimeMillis;

    @Value("${spring.datasource.validationQuery}")
    private String validationQuery;

    @Value("${spring.datasource.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.testOnReturn}")
    private boolean testOnReturn;

    @Value("${spring.datasource.poolPreparedStatements}")
    private boolean poolPreparedStatements;

    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize}")
    private int maxPoolPreparedStatementPerConnectionSize;

    @Value("${spring.datasource.filters}")
    private String filters;

    @Value("${spring.datasource.connectionProperties}")
    private String connectionProperties;

    @Value("${program.admin.name}")
    private String adminName;

    @Value("${program.admin.password}")
    private String adminPassword;

    @Value("${program.DBType}")
    private String DBType;

    @Value("${multiDownload.customer.login}")
    private String login;

    @Value("${multiDownload.customer.register}")
    private String register;

    @Value("${multiDownload.customer.openDownload}")
    private boolean openDownload;

    @Value("${multiDownload.customer.blockSize}")
    private int blockSize;

    @Value("${multiDownload.customer.dataListUrl}")
    private String dataListUrl;

    @Value("${multiDownload.customer.downloadUrl}")
    private String downloadUrl;

    @Value("${multiDownload.customer.markDownloadedUrl}")
    private String markDownloadedUrl;

    @Value("${multiDownload.customer.storeDir}")
    private String storeDir;

    @Value("${multiDownload.customer.allSyncTables}")
    private String allSyncTables;

    @Value("${multiDownload.customer.tableStruct}")
    private String tableStruct;

    @Value("${multiDownload.customer.userTable}")
    private String userTable;

    @Value("${multiDownload.customer.allSyncTableSamples}")
    private String syncSample;

    @Value("${multiDownload.customer.requestSyncTable}")
    private String requestSyncTable;

    @Value("${multiDownload.customer.changeAccount}")
    private String changeAccount;

    @Value("${multiDownload.customer.userSyncState}")
    private String userSyncState;

    private List<SyncLog> syncLogs = new LinkedList<>();

    private String token;

}
