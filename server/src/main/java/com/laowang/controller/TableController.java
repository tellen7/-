package com.laowang.controller;

import com.laowang.common.GlobalVar;
import com.laowang.common.ResultUtils;
import com.laowang.config.ScheduleTask;
import com.laowang.config.WebMvcConfig;
import com.laowang.model.DBSource;
import com.laowang.model.Result;
import com.laowang.model.TableStatus;
import com.laowang.model.User;
import com.laowang.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wangy
 */
@Controller
@Slf4j
public class TableController {

    @Autowired
    ScheduleTask task;

    @Autowired
    TableService tableService;

    @Autowired
    ScheduleTask scheduleTask;

    @Autowired
    GlobalVar globalVar;

    @RequestMapping(value = "/tableStruct")
    @ResponseBody
    public Result getTableStruct(@RequestBody TableStatus table){
        return ResultUtils.success(tableService.getTableStruct(table.getTableName()));
    }

    /**
     * 没有使用
     * @return
     */
    @RequestMapping(value = "/tables")
    @ResponseBody
    public Result getTables() {
        return ResultUtils.success(tableService.getTables());
    }

    @RequestMapping("/changeOneTable")
    @ResponseBody
    public Result changeOneTable(@RequestBody TableStatus status){
        if ("".equals(status.getSync())){
            return ResultUtils.error(-1,"修改方式不正确，sync为空");
        }
        return ResultUtils.success(tableService.changeOneTableStatus(status),"修改成功");
    }

    @RequestMapping("/allTableStatus")
    @ResponseBody
    public Result getAllTableStatus(){
        scheduleTask.syncTotalColumn();
        scheduleTask.scheduledCompress();
        return ResultUtils.success(tableService.getDBAllTableStatus());
    }

    @RequestMapping(value = "/tableSample")
    @ResponseBody
    public Result getAllUsers(@RequestBody TableStatus table){
        return ResultUtils.success(tableService.getTableData(table.getTableName()));
    }

    @RequestMapping(value = "/allSyncTableSamples")
    @ResponseBody
    public Result getAllSyncTableSamples(){
        Map<String, List> data = new HashMap<>();
        List<TableStatus> statuses = globalVar.getDBStatus();
        if (statuses != null){
            List tableStruct = null;
            List tableData = null;
            for (int i = 0; i < statuses.size(); i++) {
                TableStatus status = statuses.get(i);
                if ("1".equals(status.getSync())){
                    // 获取同步表的数据结构
                    tableStruct = tableService.getTableStruct(status.getTableName());
                    tableData = tableService.getTableData(status.getTableName());
                    tableData.add(0, tableStruct);
                    data.put(status.getTableName(), tableData);
                }
            }
        }
        return ResultUtils.success(data);
    }

    @RequestMapping(value = "/changeDataSource")
    @ResponseBody
    public Result changeDataSource(@RequestBody DBSource source) {
        if(tableService.changeJDBCDataSource(source)){
            return ResultUtils.success(tableService.getInitDBAllTableStatus(),"修改成功");
        }else{
            return ResultUtils.error(-1,"修改失败");
        }
    }

    /**
     * 获取所有同步表
     * @return
     */
    @RequestMapping("/allSyncTables")
    @ResponseBody
    public Result getAllSyncTables(){
        List<TableStatus> statuses = globalVar.getDBStatus();
        List<String> tables = null;
        if (statuses != null){
            tables = new LinkedList<>();
            for (int i = 0; i < statuses.size(); i++) {
                TableStatus status = statuses.get(i);
                if ("1".equals(status.getSync())){
                    tables.add(status.getTableName());
                }
            }
        }
        return ResultUtils.success(tables);
    }

    /**
     * 客户端查看可同步和不可同步的同步表
     * @param user
     * @return
     */
    @RequestMapping("/userTable")
    @ResponseBody
    public Result getUserTable(@RequestBody User user){
        List<TableStatus> statuses = globalVar.getDBStatus();
        Map<String,String> tables = null;
        if (statuses != null){
            tables = new HashMap<>();
            for (int i = 0; i < statuses.size(); i++) {
                TableStatus status = statuses.get(i);
                if ("1".equals(status.getSync())){
                    // get users
                    List<String> users = status.getUsers();
                    Map<String , Integer> userSyncState = status.getUserSyncState();
                    if (users != null && users.size()>0){
                        // 同步表有同步用户
                        if (users.contains(user.getUserName()) && 0 == userSyncState.get(user.getUserName()) ){
                            // 用户未同步
                            tables.put(status.getTableName(), "0");
                        }else if (users.contains(user.getUserName()) && 1 == userSyncState.get(user.getUserName())){
                            // 用户已同步
                            tables.put(status.getTableName(), "1");
                        }else if (users.contains(user.getUserName()) && 3 == userSyncState.get(user.getUserName())){
                            // 用户同步待审批
                            tables.put(status.getTableName(), "3");
                        }else if (users.contains(user.getUserName()) && 4 == userSyncState.get(user.getUserName())){
                            // 用户禁止同步
                            tables.put(status.getTableName(), "4");
                        } else {
                            // 用户无权限同步
                            tables.put(status.getTableName(), "2");
                        }
                    }else{
                        // 同步表无同步用户，用户无权限同步
                        users = new LinkedList<>();
                        status.setUsers(users);
                        tables.put(status.getTableName(), "2");
                    }
                }
            }
        }
        return ResultUtils.success(tables);
    }


    /**
     * 服务端审批某用户请求同步某张表
     * @param user
     * @return
     */
    @RequestMapping("/confirmSyncTable")
    @ResponseBody
    public Result confirmSyncTable(@RequestBody User user){
        if (user.getCode() == 0 || user.getCode() == 4) {
            return tableService.processReqeustSync(user.getTableName(), user.getUserName(), user.getCode());
        }else{
            return  ResultUtils.error(156,"审批授权码不正确，只能为0或则4（不同意）");
        }
    }

    /**
     * 某用户请求同步某张表
     * @param user
     * @return
     */
    @RequestMapping("/requestSyncTable")
    @ResponseBody
    public Result requestSyncTable(@RequestBody User user){
        return tableService.processReqeustSync(user.getTableName(), user.getUserName(), 3);

    }

    @PostMapping("/userSyncState")
    @ResponseBody
    public Result getUserSyncState(@RequestBody User user){
        return tableService.getUserSyncState(user.getUserName());
    }

    @GetMapping("/task")
    @ResponseBody
    public Result task(){
        try {
            task.doTask();
            return ResultUtils.success();
        } catch (Exception e) {
            log.error("scheduled task was wrong. {}", e);
            return ResultUtils.error(999,e.toString());
        }
    }

}
