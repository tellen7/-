package com.laowang.controller;

import com.laowang.common.DownloadResultUtils;
import com.laowang.common.FileUtil;
import com.laowang.common.GlobalVar;
import com.laowang.common.MarkUtils;
import com.laowang.model.DownloadResult;
import com.laowang.model.SyncLog;
import com.laowang.model.TableStatus;
import com.laowang.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author wangy
 */
@Controller
@Slf4j
public class DownloadController {

    @Autowired
    GlobalVar globalVar;

    /**
     * 功能：下载文件指定位置的byte数组
     */
    @RequestMapping(value = "/download", method = {RequestMethod.GET,RequestMethod.POST},produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    byte[] download(@RequestParam String startPosition,
                    @RequestParam String endPosition,
                    @RequestParam String filePath,
                    HttpServletRequest request){
        byte[] bytes = null;
        try {
            bytes = FileUtil.getFileBytesByPosition(filePath,Integer.valueOf(startPosition),Integer.valueOf(endPosition));
        } catch (NumberFormatException e) {
            log.error("===>类型转换异常,start:{} end:{}",startPosition,endPosition);
            return MarkUtils.ObjectToBytes(DownloadResultUtils.error("待下载文件位置转换成number失败"));
        }
        if (bytes == null) {
            log.info("===> filePath:{} start:{} end:{}",filePath,startPosition,endPosition);
            //文件不存在或则文件IO异常
            return MarkUtils.ObjectToBytes(DownloadResultUtils.error("文件不存在或则文件IO异常"));
        }
        log.info("===> filePath:{} start:{} end:{}",filePath,startPosition,endPosition);
        return MarkUtils.ObjectToBytes(DownloadResultUtils.success(bytes));
    }

    /**
     * 标记某个用户已经下载过某文件
     */
    @PostMapping("/markDownloaded")
    public @ResponseBody
    DownloadResult markDownloaded(@RequestParam String name, @RequestParam String tablePath, @RequestParam(required = false) String time){
        //do something to change the map in fetchDataList()
        log.info("用户{}，表路径{}",name,tablePath);
        //TODO 根据token 获取用户信息，组合用户信息和serverFile，插入数据库，表示当前用户已经下载过文件
        List<TableStatus> statuses = globalVar.getDBStatus();

        if (statuses != null) {
            for (int i = 0; i < statuses.size(); i++) {
                TableStatus status = statuses.get(i);
                // 先找到同步表（用路径查）
                if (tablePath != null && tablePath.equals(status.getFilePath())){
                    // 获取此表该同步用户同步状态
                    Map<String,Integer> s =  status.getUserSyncState();
                    if (s.get(name)!= null) {
                        s.put(name,1);
                        // 更新同步日志
                        List<SyncLog> logs = globalVar.getSyncLogs();
                        for (int j = 0; j < logs.size(); j++) {
                            // 当表名和用户名对应的日志存在时，则更新time入库耗时字段
                            if (status.getTableName().equals(logs.get(j).getTable()) && name.equals(logs.get(j).getUser())){
                                // 该文件已经被成功下载，现在添加入库耗时字段
                                logs.get(j).setTime(time);
                                return DownloadResultUtils.success();
                            }
                        }
                        // 上述检查没有找到该同步日志，则添加新的同步日志
                        SyncLog log = new SyncLog();
                        log.setUser(name);
                        log.setTable(status.getTableName());
                        //设置日期格式
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        log.setData(df.format(new Date()));
                        if (!StringUtils.isEmpty(time)){
                            log.setTime(time);
                        }
                        globalVar.getSyncLogs().add(log);
                        // 更新总的成功同步的计数器
                        globalVar.setSyncCounter(globalVar.getSyncCounter()+1);
                     }
                }
            }
        }

        return DownloadResultUtils.success();
    }

    /**
     *  获取待下载文件列表（文件位置和长度）
     */
    @PostMapping("/fetchDataList")
    public @ResponseBody
    DownloadResult fetchDataList(@RequestParam String name){
        List<TableStatus> statuses = globalVar.getDBStatus();

        
        //filePath<---->size
        Map<String, Object> list = new HashMap<>();
        if (statuses != null) {
            for (int i = 0; i < statuses.size(); i++) {
                // 遍历所有表，查找到用户需要同步的所有表，构造出路径与文件大小映射关系返回客户端
                TableStatus status = statuses.get(i);
                List<String> users = status.getUsers();
                if (users != null && users.size() > 0) {
                    for (int j = 0; j < users.size(); j++) {
                        if (name.equals(users.get(j)) && "1".equals(status.getSync())) {
                            // 获取到当前用户的表的同步状态，0表示该表该用户没有同步，1则已同步
                            Map<String,Integer> s =  status.getUserSyncState();
                            if (s.get(name) ==0) {
                                list.put(status.getFilePath(), FileUtil.getFileLength(status.getFilePath()));
                            }
                        }
                    }
                }
            }
        }
        return DownloadResultUtils.success(list);
    }



    /**
     * 废弃
     * 功能： 返回文件的大小，下载文件的下载url；或则文件不存在
     */
    @GetMapping(value = "/size",produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    DownloadResult getSizeOfFile(@RequestParam String filePath){
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()){
            return DownloadResultUtils.error("文件不存在或则请求的是一个文件夹");
        }
        long size = file.length();
        log.info("===>filePath: {} 长度：{}",filePath,size);
        return DownloadResultUtils.success(size);
    }

}
