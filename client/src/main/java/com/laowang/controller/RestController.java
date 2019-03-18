package com.laowang.controller;

import com.laowang.model.DBSource;
import com.laowang.model.Result;
import com.laowang.model.SyncTable;
import com.laowang.model.User;
import com.laowang.service.TableService;
import com.laowang.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wangy
 */
@Controller
@Slf4j
public class RestController {

    @Autowired
    TableService tableService;

    /**
     * 修改数据源接口
     *
     * @param source
     * @return
     */
    @RequestMapping(value = "/changeDataSource")
    @ResponseBody
    public Result changeDataSource(@RequestBody DBSource source) {
        if (tableService.changeJDBCDataSource(source)) {
            return ResultUtils.success("修改成功");
        } else {
            return ResultUtils.error(-1, "修改失败");
        }
    }


    @PostMapping(value = "/requestSyncTable")
    @ResponseBody
    public Result requestSyncTable(@RequestBody SyncTable table) {
        return tableService.requestSyncTable(table.getTableName());
    }

    @PostMapping(value = "/changeAccount")
    @ResponseBody
    public Result changeAccount(@RequestBody User user) {
        return tableService.changeAccount(user);
    }

    @PostMapping(value = "/login")
    @ResponseBody
    public Result login(@RequestBody User user){
        return tableService.login(user);
    }

    @PostMapping(value = "/register")
    @ResponseBody
    public Result register(@RequestBody User user){
        return tableService.register(user);
    }
}
