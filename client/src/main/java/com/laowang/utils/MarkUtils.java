package com.laowang.utils;

import com.laowang.model.DBSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wangyonghao
 */
@Slf4j
public class MarkUtils {

    /**获取对象的byte数组，出现异常返回null*/
    public static byte[] objectToBytes(Object var){
        if (var == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(var);

            byte[] bytes = bo.toByteArray();

            bo.close();
            oo.close();

            return bytes;
        } catch (IOException e) {
            log.error("===>结果对象序列化异常");
            e.printStackTrace();
            return null;
        }
    }

    /**byte数组转换成对象，出现异常返回null*/
    public static Object bytesToObject(byte[] bytes){
        Object obj = null;
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();

            bi.close();
            oi.close();

        } catch (IOException e) {
            log.error("===>结果对象序列化异常");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            log.error("===>反序列化没找到对应类");
            e.printStackTrace();
        }
        return obj;
    }


//    public boolean isTableExistInSQLServer(String tableName){
//        String sql = "select * from dbo.sysobjects where id = object_id(N'[dbo].[user111]') and OBJECTPROPERTY(id, N'IsUserTable') = 1";
//
//    }

    /**
     * 测试用户修改的数据源是否可用
     * @param source
     * @return
     */
    public static boolean checkDBSource(DBSource source){
        Connection conn = null;
        Statement stat = null;
        try {
            Class.forName(source.getDriverName());
            // 如果能获得connection就能连接数据库
            conn = DriverManager.getConnection(source.getUrl(),source.getUsername(),source.getPassword());
            stat = conn.createStatement();

            return true;
        } catch (ClassNotFoundException e) {
            log.error("驱动配置错误或则没有该驱动，暂不支持{}",e);
            return false;
        } catch (SQLException e) {
            log.error("SQLException {}",e);
            return false;
        }finally {
            if (stat != null){
                try {
                    stat.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从压缩文件路径中提取表名
     * @param filepath
     * @return 表名字符串
     */
    public static String extractTableName(String filepath){
        if (!StringUtils.isEmpty(filepath)){
            String file = filepath.substring(filepath.lastIndexOf('\\')+1);
            return file.split("\\.")[0];
        }
        return null;
    }

    /**
     * 将包含表结构信息的list转换成sql字符串
     * @param temp
     * @param tableName
     * @return 相应的建表语句
     */
    public static String listToSql(List temp, String tableName){
        if (temp == null || temp.size() == 0){
            return null;
        }
        StringBuffer sql = new StringBuffer("create table "+ tableName + " ( \n");
        for (int i = 0; i < temp.size(); i++) {
            if (i == temp.size()-1){
                sql.append(temp.get(i) + " varchar(255)\n");
            }else {
                sql.append(temp.get(i) + " varchar(255),\n");
            }
        }
        sql.append(")");
        return  sql.toString();
    }


    public static void main(String[] args) {
        DBSource dbSource = new DBSource();
        dbSource.setDriverName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dbSource.setDBType("sqlServer");
        dbSource.setUrl("jdbc:sqlserver://localhost:1433;databaseName=duplication");
        dbSource.setUsername("root");
        dbSource.setPassword("1234");
        System.out.println(checkDBSource(dbSource));
    }


    public static List getSixLatestLogs(List logs){
        List latest = null;
        int count = 0;
        if (logs != null && logs.size() > 0) {
            latest = new LinkedList();
            for (int i = logs.size()-1; i >=0 ; i--) {
                count++;
                latest.add(logs.get(i));
                if (count == 6){
                    break;
                }
            }
        }

        return  latest;
    }

}