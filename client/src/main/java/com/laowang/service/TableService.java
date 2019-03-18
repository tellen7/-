package com.laowang.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laowang.config.DruidDBConfig;
import com.laowang.model.*;
import com.laowang.start.RefreshToken;
import com.laowang.utils.MarkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wangy
 */
@Service
@Slf4j
public class TableService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DruidDataSource dataSource;
    @Autowired
    private GlobalVar globalVar;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RefreshToken refreshToken;


    /**
     * 功能： 修改数据源
     *
     * @param s 要修改的数据源信息
     * @return 1：测试连接成功并且更新数据源；0测试数据库连接失败
     */
    public boolean changeJDBCDataSource(DBSource s) {
        log.info("DType:{}, url:{}, username:{}, password:{}", s.getDBType(), s.getUrl(), s.getUsername(), s.getPassword());
        switch (s.getDBType()) {
            case "mysql":
                s.setDriverName("com.mysql.jdbc.Driver");
                s.setValidationQueryString("select 1");
                break;
            case "oracle":
                s.setDriverName("oracle.jdbc.driver.OracleDriver");
                s.setValidationQueryString("select 1 from dual");
                break;
            case "sqlserver":
                s.setDriverName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                s.setValidationQueryString("select 1");
                break;
            case "postgresql":
                s.setDriverName("org.postgresql.Driver");
                s.setValidationQueryString("select version()");
                break;
        }
        //test connection here
        if (MarkUtils.checkDBSource(s)) {
            globalVar.setDBType(s.getDBType());
            globalVar.setUsername(s.getUsername());
            globalVar.setPassword(s.getPassword());
            globalVar.setDbUrl(s.getUrl());
            globalVar.setDriverClassName(s.getDriverName());

            DruidDataSource datasource1 = new DruidDataSource();

            datasource1.setUrl(globalVar.getDbUrl());
            datasource1.setUsername(globalVar.getUsername());
            datasource1.setPassword(globalVar.getPassword());
            datasource1.setDriverClassName(globalVar.getDriverClassName());

            //configuration
            datasource1.setInitialSize(globalVar.getInitialSize());
            datasource1.setMinIdle(globalVar.getMinIdle());
            datasource1.setMaxActive(globalVar.getMaxActive());
            datasource1.setMaxWait(globalVar.getMaxWait());
            datasource1.setTimeBetweenEvictionRunsMillis(globalVar.getTimeBetweenEvictionRunsMillis());
            datasource1.setMinEvictableIdleTimeMillis(globalVar.getMinEvictableIdleTimeMillis());
            datasource1.setValidationQuery(globalVar.getValidationQuery());
            datasource1.setTestWhileIdle(globalVar.isTestWhileIdle());
            datasource1.setTestOnBorrow(globalVar.isTestOnBorrow());
            datasource1.setTestOnReturn(globalVar.isTestOnReturn());
            datasource1.setPoolPreparedStatements(globalVar.isPoolPreparedStatements());
            datasource1.setMaxPoolPreparedStatementPerConnectionSize(globalVar.getMaxPoolPreparedStatementPerConnectionSize());
            try {
                datasource1.setFilters(globalVar.getFilters());
            } catch (SQLException e) {
                log.error("druid configuration initialization filter", e);
            }
            datasource1.setConnectionProperties(globalVar.getConnectionProperties());
            datasource1.setValidationQuery(s.getValidationQueryString());
            try {
                datasource1.init();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            jdbcTemplate.setDataSource(datasource1);
            dataSource = datasource1;
            log.info("数据源修改成功");
            return true;
        } else {
            log.warn("数据源修改失败");
            return false;
        }
    }


    /**
     * 获取所有表
     *  有两次网络请求
     * @return
     */
    public List<SyncTable> getUserTable() {
        List<SyncTable> list = new LinkedList<>();
        Map<String, String> states = null;
        Map<String,List<List<String>>> datas = null;
        JSONObject postData = new JSONObject();
        postData.put("userName", globalVar.getAdminName());
        // 获取所有同步表状态
        JSONObject tableState = restTemplate.postForObject(globalVar.getUserTable(), postData, JSONObject.class);
        JSONObject resultData = tableState.getJSONObject("data");
        if (resultData == null || "".equals(resultData.toJSONString()) || "".equals(resultData.toString())){
            return null;
        }
        states = tableState.getJSONObject("data").toJavaObject(Map.class);
        log.info("获取所有表{}",JSONObject.toJSON(states));
        // 获取所有同步表数据样例
        JSONObject tableData = restTemplate.postForObject(globalVar.getSyncSample(), postData, JSONObject.class);
        datas = tableData.getJSONObject("data").toJavaObject(Map.class);
        log.info(JSONObject.toJSONString(datas));
        for (Map.Entry<String, String> entry : states.entrySet()){
            SyncTable table = new SyncTable();
            table.setTableName(entry.getKey());
            String state = entry.getValue();
            switch (state){
                // 用户没有同步
                case "0" : table.setState("未同步");break;
                // 用户已经同步完成
                case "1" : table.setState("已同步");break;
                // 用户没权限同步此表
                case "2" : table.setState("无权限");break;
                // 用户同步请求待审批
                case "3" : table.setState("待审批");break;
                // 用户禁止同步此表
                case "4" : table.setState("禁同步");break;
                default:
            }
            table.setSample(datas.get(entry.getKey()));
            list.add(table);
        }
        log.info("所有同步表的样例数据{}", JSONObject.toJSONString(list));
        return list;
    }


    /**
     * 请求同步某张表
     *
     * @param table
     */
    public Result requestSyncTable(String table) {
        JSONObject postData = new JSONObject();
        postData.put("tableName", table);
        postData.put("userName", globalVar.getAdminName());
        JSONObject data = restTemplate.postForObject(globalVar.getRequestSyncTable(), postData, JSONObject.class);

        return data.toJavaObject(Result.class);
    }


    /**
     * 请求同步某张表
     *
     */
    public Result changeAccount(User user) {

        user.setUserName(globalVar.getAdminName());
        user.setToken(globalVar.getToken());
        JSONObject data = restTemplate.postForObject(globalVar.getChangeAccount(), JSONObject.toJSON(user), JSONObject.class);

        Result result = data.toJavaObject(Result.class);
        if (result.getCode() == 0){
            // 密码成功修改后，更新缓存的token
            log.info("{}成功修改了密码", user.getUserName());
            refreshToken.refresh();
        }
        return result;
    }


    public Map<String, Integer> getUserSyncState(){
        Map<String, Integer> map = null;
        JSONObject postData = new JSONObject();
        postData.put("userName", globalVar.getAdminName());
        JSONObject data = restTemplate.postForObject(globalVar.getUserSyncState(), postData, JSONObject.class);
        if (data.getIntValue("code") == 0){
            map = new HashMap<>();
            map.put("sync", data.getJSONObject("data").getIntValue("sync"));
            map.put("total", data.getJSONObject("data").getIntValue("total"));
            map.put("task", data.getJSONObject("data").getIntValue("task"));
            map.put("approval", data.getJSONObject("data").getIntValue("approval"));
            log.info(JSONObject.toJSONString(map));
        }

        return map;
    }


    public Result login(User user){
        JSONObject data = restTemplate.postForObject(globalVar.getUserSyncState(), JSONObject.toJSON(user), JSONObject.class);
        return null;
    }



    public Result register(User user){
        JSONObject data = restTemplate.postForObject(globalVar.getUserSyncState(), JSONObject.toJSON(user), JSONObject.class);
        return null;
    }
}
