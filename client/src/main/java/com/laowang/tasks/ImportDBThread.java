package com.laowang.tasks;

import com.alibaba.fastjson.JSONObject;
import com.laowang.model.*;
import com.laowang.utils.FileUtils;
import com.laowang.utils.MarkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class ImportDBThread  implements Runnable{

    @Autowired
    GlobalVar globalVar;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run() {
        try {
            while (true) {
                String file = ControlBean.importDBList.take();
                if (!"".equals(file) && file != null){
                    String[] urls = file.split("\\|");
                    file = urls[0];
                    String tableName = MarkUtils.extractTableName(file);
                    String targetFile = FileUtils.unGzipFile(file);
                    //创建数据表结构，存在抛异常并捕获，不存在新建
                    createTable(tableName);
                    log.info("数据库表准备完成");
                    //导入target文件入库
                    long start = System.currentTimeMillis();
                    importDataToDB(targetFile, tableName);
                    long end = System.currentTimeMillis();
                    log.info("数据导入完成,耗时{}ms",end-start);

                    // 本地日志记录
                    SyncLog log = new SyncLog();
                    log.setTable(tableName);
                    log.setTime(String.valueOf(end-start));
                    //设置日期格式
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    log.setDate(df.format(new Date()));
                    globalVar.getSyncLogs().add(log);

                    // 告诉服务端入库耗时
                    markFileDownloaded(globalVar.getAdminName(), urls[1], end-start);
                }
            }
        }catch (Exception e){
            log.error("导入数据库线程发生错误,{}", e);
        }
    }


    /**
     * 创建表
     * DB2, Derby, H2, HSQL, Informix, MS-SQL, MySQL, Oracle, PostgreSQL, Sybase, Hana
     * @param tableName
     */
    private void createTable(String tableName){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization",globalVar.getToken());
        List<MediaType> mediaTypes = new LinkedList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);

        JSONObject postData = new JSONObject();
        postData.put("tableName", tableName);

        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(postData, headers);
        ResponseEntity<Result> response = null;
        try {
            //可能会出现请求异常
            response = restTemplate.postForEntity(globalVar.getTableStruct(), httpEntity, Result.class);
            Result result = response.getBody();
            if (result.getCode() == 0) {
                List data = (List) result.getData();
                String sql = MarkUtils.listToSql(data, tableName);
                //执行建表语句
                log.info("执行建表语句,{}",sql);
                jdbcTemplate.execute(sql);
            } else {
                log.error("获取待入库的表结构出错");
            }
        } catch (RestClientException e) {
            log.error("===>网络异常，获取表结构失败...", e);
        } catch (Exception e){
            log.error("系统出现异常，{}", e);
        }
    }

    /**
     * 导入数据库
     * @param filePath
     * @param tableName
     */
    public void importDataToDB(String filePath, String tableName){
        StringBuffer sqlSb;
        String sql;
        switch (globalVar.getDBType()) {
            case "mysql":   sqlSb = new StringBuffer("LOAD DATA LOCAL INFILE '");
            sqlSb.append(filePath);
            sqlSb.append("' into table duplication.");
            sqlSb.append(tableName);
            sqlSb.append(" FIELDS TERMINATED BY ',' ");
            sqlSb.append(" LINES TERMINATED BY '\\n' ");
            sql = sqlSb.toString();
            log.info("导入数据库命令: {}", sql);
            jdbcTemplate.execute(sql);break;
            case "sqlserver": sqlSb = new StringBuffer("bulk insert ");
            sqlSb.append(tableName);
            sqlSb.append(" from '"+filePath);
            sqlSb.append("' with ( fieldterminator=',', ROWTERMINATOR='0x0a')");
            sql = sqlSb.toString();
            log.info("导入数据库命令: {}", sql);
            jdbcTemplate.execute(sql);break;
        }
    }



    public void markFileDownloaded(String name, String filePath, long time){
        //设置请求
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        //设置token
        headers.add("Authorization",globalVar.getToken());
        param.add("name", name);
        param.add("tablePath", filePath);
        param.add("time", String.valueOf(time));
        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(param,headers);

        try {
            ResponseEntity<DownloadResult> response = restTemplate.exchange(globalVar.getMarkDownloadedUrl(), HttpMethod.POST,httpEntity, DownloadResult.class);
            if (response.getBody().getCode() == DownloadResult.StatusCode.SUCCESS){
                log.info("===>标记服务端文件入库状态完成");
            }else{
                log.info("===>标记服务端文件入库状态失败");
            }
        } catch (RestClientException e) {
            log.error("===>标记服务端文件入库状态异常 {}"+e);
        }
    }
}
