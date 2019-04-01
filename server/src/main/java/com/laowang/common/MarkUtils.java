package com.laowang.common;

import com.laowang.model.DBSource;
import com.laowang.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class MarkUtils {

    /**获取对象的byte数组，出现异常返回null*/
    public static byte[] ObjectToBytes(Object var){
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

    /**byte数组转换成对象*/
    public static Object BytesToObject(byte[] bytes){
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



    //token密钥
    private static String key= "laowang";

    /**
     * 从token提取用户名
     * @param token
     * @return
     */
    public static String getUserNameByToken(String token){
        String userName;
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            userName = claims.getSubject();
        }catch (Exception e) {
            log.warn("token异常,name");
            userName = null;
        }
        return userName;
    }


    /**
     * 从token提取用户id
     * @param token
     * @return
     */
    public static String getUserPasswordByToken(String token){
        String password;
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
            password = claims.get("password",String.class);
        }catch (Exception e) {
            log.warn("token异常,id");
            password = null;
        }
        return password;
    }

    /**
     * 创建token
     * @param keyGen
     * @return
     */
    public static String initToken(User user, String keyGen){
        //生成数据声明claims
        Map<String,Object> claims = new HashMap<>();
        claims.put("sub",user.getUserName());
        claims.put("password",user.getPassword());
        claims.put("keyGen",keyGen);
        String Token = Jwts.builder().setClaims(claims)
                //加密算法
                .signWith(SignatureAlgorithm.HS512,key).compact();
        return Token;
    }


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




    public static void main(String[] args) throws IOException {
//        DownloadResult result = new DownloadResult("序列化",DownloadResult.StatusCode.SUCCESS,"this is a data");
//        byte[] bytes = ObjectToBytes(result);
//        System.out.println(JSONObject.toJSONString(bytes));
//
//        DownloadResult temp = (DownloadResult) BytesToObject(bytes);
//        System.out.println(JSONObject.toJSONString(temp));
//
//        DBSource source = new DBSource();
//        source.setDriverName("com.mysql.jdbc.Driver");
//        source.setDBType("mysql");
//        source.setUrl("jdbc:mysql://127.0.0.1:3306/springboot?useUnicode=true&characterEncoding=utf-8&useSSL=false");
//        source.setUsername("root");
//        source.setPassword("wyh000000");
//        System.out.println(checkDBSource(source));
//        unGzipFile("D:\\export\\mysql\\article.13.csv.gzip","D:\\export\\mysql\\article.csv");

//        File file = new File("D:\\export\\user.csv");
//        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
//        for (int i = 2; i < 5000000; i++) {
//            outputStreamWriter.write(i+",\"643710049@qq.com\",\"Wed Jun 13 13:06:44 CST 2018\",\"123\",\"laowang\",\"this is a test\"\n");
//        }
//        outputStreamWriter.flush();
//        outputStreamWriter.close();


        DBSource source = new DBSource();
        source.setDriverName("oracle.jdbc.driver.OracleDriver");
        source.setUsername("c##wyh");
        source.setPassword("wyh000000");
        source.setUrl("jdbc:oracle:thin:@127.0.0.1:1521:orcl");
        checkDBSource(source);
    }


}
