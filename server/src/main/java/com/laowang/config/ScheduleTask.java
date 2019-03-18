package com.laowang.config;

import com.laowang.common.FileUtil;
import com.laowang.common.GlobalVar;
import com.laowang.model.TableStatus;
import com.laowang.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author wangy
 */
@Component
@Slf4j
public class ScheduleTask implements CommandLineRunner {

    @Autowired
    GlobalVar globalVar;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TableService tableService;

    /**
     * corn表达式，计划每天凌晨3点执行压缩
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledCompress(){
        log.info("执行定时压缩同步数据表任务");
        List<TableStatus> statuses = globalVar.getDBStatus();
        if (statuses == null){
            log.info("没有数据表，不能执行定时压缩同步数据表任务");
            return;
        }
        for (int i = 0; i < statuses.size(); i++) {
            TableStatus table = statuses.get(i);
            String sql = null;
            if ("1".equals(table.getSync())){
                //根据数据库类型执行导出数据命令
                if (table.getTotalColumn() == table.getCompressColumn()){
                    continue;
                }
                String filePath = globalVar.getStoreDir() + table.getTableName()+ "."+table.getTotalColumn()+".csv";
                switch (globalVar.getDBType()){
                    case "mysql" : sql = "SELECT * INTO OUTFILE '" + filePath +
                            "' FIELDS TERMINATED BY ',' " +
                            "LINES TERMINATED BY '\\n' " +
                            "FROM "+table.getTableName()+
                            " limit "+table.getCompressColumn()+", "+table.getTotalColumn();break;
                    case "sqlserver":sql = "EXEC [master]..xp_cmdshell 'BCP duplication.dbo."+
                            table.getTableName()+ " out "+
                            filePath + " -c -t\",\" -r\"\\n\" -T'";break;
                }
                jdbcTemplate.execute(sql);
                //将导出的数据文件压缩，并把记录缓存到本地
                try {
                    FileUtil.gzipFile(filePath,filePath+".gzip");
                    table.setFilePath(filePath+".gzip");
                } catch (IOException e) {
                    log.error("压缩文件异常,{}", e);
                }

            }
        }
    }

    /**
     * 每天12点更新所有表的行数任务
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void syncTotalColumn(){
        log.info("执行定时更新所有表总行数任务");

        List<TableStatus> statuses = globalVar.getDBStatus();
        if (statuses == null){
            log.info("没有数据表，不能执行定时更新所有表总行数任务");
            return;
        }
        for (int i = 0; i < statuses.size(); i++) {
            TableStatus table = statuses.get(i);
            String sql = null;

                //根据数据库类型查询所有行，TODO 这里应该只统计同步表的行
                switch (globalVar.getDBType()){
                    case "sqlserver":
                    case "mysql" : sql = "SELECT count(*) " +
                            "FROM "+table.getTableName();break;
                }
                List<Object> total = jdbcTemplate.query(sql,  new RowMapper<Object>() {

                    @Override
                    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                        return resultSet.getInt(1);
                    }
                });
                Integer totalColumn = (Integer) total.get(0);
                table.setTotalColumn(totalColumn);
            }
    }

    @Override
    public void run(String... strings) throws Exception {
        // 系统启动后，把数据缓存
        tableService.getInitDBAllTableStatus();
        log.info("系统数据初始化成功");
    }

    public void doTask(){
        syncTotalColumn();
        scheduledCompress();
    }
}
