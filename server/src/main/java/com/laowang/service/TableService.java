package com.laowang.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.laowang.common.GlobalVar;
import com.laowang.common.MarkUtils;
import com.laowang.common.ResultUtils;
import com.laowang.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author wangy
 */
@Service
@Slf4j
public class TableService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private GlobalVar globalVar;

    /**
     * 功能： 修改数据源
     *
     * @param s 要修改的数据源信息
     * @return 1：测试连接成功并且更新数据源；0测试数据库连接失败
     */
    public boolean changeJDBCDataSource(DBSource s) {
        log.info("DType:{}, url:{}, username:{}, password:{}", s.getDBType(), s.getUrl(), s.getUsername(), s.getPassword());
        if (StringUtils.isEmpty(s.getDBType()) || StringUtils.isEmpty(s.getUrl()) || StringUtils.isEmpty(s.getUsername()) || StringUtils.isEmpty(s.getPassword())) {
            return false;
        }
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
            case "PostgreSQL":
                s.setDriverName("org.postgresql.Driver");
                s.setValidationQueryString("select version()");
                break;
                default:
        }
        //test connection here
        if (MarkUtils.checkDBSource(s)) {
            globalVar.setDBType(s.getDBType());
            globalVar.setUsername(s.getUsername());
            globalVar.setPassword(s.getPassword());
            globalVar.setDbUrl(s.getUrl());
            globalVar.setDriverClassName(s.getDriverName());
            globalVar.setValidationQuery(s.getValidationQueryString());

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
            // 更新和数据库相关的状态
            globalVar.setSyncCounter(0);
            globalVar.setSyncLogs(new LinkedList<SyncLog>());
            return true;
        } else {
            log.warn("数据源修改失败");
            return false;
        }
    }


    /**
     * 功能： 获取表前10行数据
     *
     * @param tableName
     * @return
     */
    public List getTableData(String tableName) {
        //
        String sql = null;
        switch (globalVar.getDBType()){
            case "mysql": sql = "SELECT * FROM " + tableName + " LIMIT 5;";break;
            case "sqlserver":sql = "SELECT TOP 5 * FROM "+tableName+" ;";break;
            case "oracle":sql = "SELECT * FROM "+ tableName +"  WHERE ROWNUM <= 5;";break;
            default:
        }
        log.info("获取前世行表数据,sql: {}", sql);
        List<String> columns = this.getTableStruct(tableName);
        if (columns.size() == 0) {
            return null;
        }
        return jdbcTemplate.query(sql, (resultSet, i) -> {
            List items = new LinkedList();
            for (int j = 0; j < columns.size(); j++) {
                items.add(resultSet.getObject(columns.get(j)));
            }
            return items;
        });
    }


    /**
     * 功能： 获取表结构
     *
     * @param tableName 表名
     * @return 表结构信息（只有列信息，索引等信息不查询）
     */
    public List getTableStruct(String tableName) {
        //
        List result = new LinkedList<>();
        if ("mysql".equals(globalVar.getDBType())) {
            String sql = "SELECT * FROM " + tableName + " LIMIT 1;";
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
            SqlRowSetMetaData metaData = rowSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                result.add(metaData.getColumnName(i));
            }
        }else if ("sqlserver".equals(globalVar.getDBType())){
            String sql = "SELECT * FROM SysColumns Where id=Object_Id('"+tableName+"');";
            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
            while (rowSet.next()){
                result.add(rowSet.getString("name"));
            }
        }
        return result;
    }


    /**
     * 功能： 获取所有表的表名
     *
     * @return schema----table的映射list
     */
    public List getTables() {

        return jdbcTemplate.query("select table_name, table_schema from information_schema.tables where  TABLE_TYPE = 'BASE TABLE'", new RowMapper<Map>() {

            @Override
            public Map mapRow(ResultSet resultSet, int i) throws SQLException {
                Map row = new HashMap();
                row.put(resultSet.getString("table_schema"), resultSet.getString("table_name"));
                return row;
            }
        });
    }


    /**
     * 初始化数据库表的同步状态
     *
     * @return
     */
    public List getInitDBAllTableStatus() {
        List tables = this.getTables();

        List<TableStatus> result = new LinkedList<>();

        for (int i = 0; i < tables.size(); i++) {
            // 封装tableStatus
            TableStatus status = new TableStatus();
            Map map = (Map) tables.get(i);
            for (Object key : map.keySet()) {
                status.setTableName((String) map.get(key));
                status.setTableSchema((String) key);
                break;
            }
            status.init();
            // 添加到结果集
            result.add(status);
        }
        // 缓存到项目
        globalVar.setDBStatus(result);
        return result;
    }


    /**
     * 获取数据库所有表同步状态
     *
     * @return
     */
    public List getDBAllTableStatus() {
        if (globalVar.getDBStatus() != null) {
            return globalVar.getDBStatus();
        } else {
            return this.getInitDBAllTableStatus();
        }
    }


    /**
     * 更新一张表的同步状态，只更新sync和users（配置表的同步与否与同步用户）
     *
     * @param status
     * @return
     */
    public List changeOneTableStatus(TableStatus status) {
        log.info("table params, {}", JSON.toJSON(status));
        List<TableStatus> result = globalVar.getDBStatus();
        if (result == null) {
            globalVar.setDBStatus(this.getInitDBAllTableStatus());
            result = globalVar.getDBStatus();
        }
        for (int i = 0; i < result.size(); i++) {
            TableStatus item = result.get(i);
            // 找到要更新的表
            if (item.getTableName().equals(status.getTableName())) {
                item.setSync(status.getSync());
                // 检查新的同步用户组数据是否合法
                if (status.getUsers() != null && status.getUsers().contains("")) {
                    log.warn("前端传来空用户，省略");
                    status.getUsers().remove("");
                }
                // 用户组发生变化
                if (status.getUsers() != null && status.getUsers().size() > 0) {
                    item.setUsers(status.getUsers());

                    Map<String, Integer> state = item.getUserSyncState();
                    Map<String, Integer> newState = new HashMap<>();
                    for (int j = 0; j < status.getUsers().size(); j++) {
                        // 如果是老用户，不更新同步状态并将老用户的同步状态复制
                        if (state != null && state.containsKey(status.getUsers().get(j))) {
                            newState.put(status.getUsers().get(j), state.get(status.getUsers().get(j)));
                        } else {
                            // 如果是新用户，添加用户
                            newState.put(status.getUsers().get(j), 0);
                        }
                    }
                    item.setUserSyncState(newState);
                } else {
                    item.setUsers(null);
                    item.setUserSyncState(null);
                }
                break;
            }
        }

        return globalVar.getDBStatus();
    }


    /**
     * 获取所有用户——————表关系
     * @return
     */
    public List getAllSyncRequest() {
        List<SyncRequestMessage> messages = new LinkedList<>();
        List<TableStatus> tables = globalVar.getDBStatus();
        if (tables != null && tables.size() > 0) {
            for (int i = 0; i < tables.size(); i++) {
                TableStatus table = tables.get(i);
                Map<String, Integer> userSyncState = table.getUserSyncState();
                if (userSyncState != null && userSyncState.size() > 0) {
                    for (Map.Entry<String, Integer> entry : userSyncState.entrySet()) {
                        SyncRequestMessage message = new SyncRequestMessage();
                        message.setTableName(table.getTableName());
                        message.setSync(table.getSync());
                        message.setUser(entry.getKey());
                        message.setCode(entry.getValue());
                        messages.add(message);
                    }
                }
            }
        }
        return messages;
    }


    public Result processReqeustSync(String tableName, String userName, int code) {
        List<TableStatus> statuses = globalVar.getDBStatus();
        if (statuses != null) {
            for (int i = 0; i < statuses.size(); i++) {
                TableStatus status = statuses.get(i);
                if ("1".equals(status.getSync())) {
                    // get users
                    if (tableName.equals(status.getTableName())) {
                        List<String> users = status.getUsers();
                        Map<String, Integer> userSyncState = status.getUserSyncState();
                        if (users != null && users.size() > 0) {
                            if (users.contains(userName)) {
                                int state = userSyncState.get(userName);
                                if (state == 0 && code ==0 ){
                                    return ResultUtils.error(225,"非法操作，已是未同步用户");
                                }else if (state == 3 && code == 3 ){
                                    return ResultUtils.error(226,"已是请求状态，请等待审批，不要重复请求");
                                }else if (state == 4 && code == 4 ){
                                    return ResultUtils.error(227,"已是禁止同步，不要重复操作");
                                }else if (state == 0 && code == 3 ){
                                    return ResultUtils.error(233, "已经是同步用户，不可再添加");
                                }else if (state == 4 && code == 3 ){
                                    return ResultUtils.error(226,"已被禁止同步，请联系服务端管理员");
                                }
                                // 用于更新状态
                                userSyncState.put(userName, code);
                            }else {
                                users.add(userName);
                                userSyncState.put(userName, code);
                            }
                        } else {
                            users = new LinkedList<>();
                            users.add(userName);
                            status.setUsers(users);
                            userSyncState = new HashMap<>();
                            userSyncState.put(userName, code);
                            status.setUserSyncState(userSyncState);
                        }
                        return ResultUtils.success();
                    }
                }
            }
        }
        return ResultUtils.error(111, "不是同步表或则请求表不存在");
    }


    public Map<String, Integer> getDataForIndex(){
        Map<String, Integer> data = new HashMap<>();
//        HashSet<String> totalUser = new HashSet<>();
        int totalTable = 0;
        int totalSyncTask = 0;
        int totalSyncSuccess = globalVar.getSyncCounter();

        List<TableStatus> statuses = globalVar.getDBStatus();
        if (statuses != null) {
            //
            totalTable = statuses.size();
            for (int i = 0; i < statuses.size(); i++) {
                Map<String, Integer> state = statuses.get(i).getUserSyncState();
                if (state == null || state.size() == 0){
                    continue;
                }
                for (Map.Entry<String, Integer> entry : state.entrySet()){
                    // 将user add进set集合
//                    totalUser.add(entry.getKey());
                    // 统计待同步任务量
                    if (entry.getValue() == 0){
                        totalSyncTask ++;
                    }
                }
            }
        }

//        data.put("user", totalUser.size());
        data.put("user", globalVar.getUsers().size());
        data.put("table", totalTable);
        data.put("task", totalSyncTask);
        data.put("counter", totalSyncSuccess);
        return data;
    }

    /**
     * 客户端获取同步状态：请求同步数----待审批状态，同步表数----和用户相关的同步状态的表，同步任务数----用户待同步表数, 同步总量
     * @param userName
     * @return
     */
    public Result getUserSyncState(String userName) {
        // 待审批
        int approval = 0;
        // 同步表
        int sync = 0;
        // 同步任务
        int task = 0;
        // 同步总量
        int total = 0;

        List<TableStatus> statuses = globalVar.getDBStatus();
        if (statuses == null || statuses.size() == 0){
            return ResultUtils.error(65,"服务端没有初始化");
        }

        for (int i = 0; i < statuses.size(); i++) {
            TableStatus status = statuses.get(i);
            if ("1".equals(statuses.get(i).getSync())){
                sync++;
                if (status.getUsers() != null && status.getUsers().contains(userName)) {
                    int code = status.getUserSyncState().get(userName);
                    // 被禁止同步的表不统计
                    if (code == 4) {
                        continue;
                    }
                    switch (code) {
                        case 0:
                            task++;
                            break;
                        case 1:
                            total++;
                            break;
                        case 3:
                            approval++;
                            break;
                        default:
                    }
                }
            }
        }

        Map data = new HashMap();
        data.put("sync", sync);
        data.put("total", total);
        data.put("task", task);
        data.put("approval", approval);

        return ResultUtils.success(data);
    }
}
