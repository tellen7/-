package com.laowang.controller;

import com.laowang.common.GlobalVar;
import com.laowang.common.MarkUtils;
import com.laowang.common.ResultUtils;
import com.laowang.model.Result;
import com.laowang.model.User;
import com.laowang.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wangy
 */
@Controller
@Slf4j
public class PageController {

    @Autowired
    private GlobalVar globalVar;
    @Autowired
    private TableService tableService;

    /**
     * 系统主页
     * @return
     */
    @GetMapping("/index")
    public ModelAndView createForm(Model model, HttpServletRequest request) {
        String token = request.getParameter("token");
        String userName = MarkUtils.getUserNameByToken(token);
        String password = MarkUtils.getUserPasswordByToken(token);
        model.addAttribute("user",userName);
        if (globalVar.getAdminName().equals(userName) && globalVar.getAdminPassword().equals(password)){
            log.info("用户{}访问个人主页成功", userName);
            Map<String, Integer> data = tableService.getDataForIndex();
            model.addAttribute("users",data.get("user"));
            model.addAttribute("table",data.get("table"));
            model.addAttribute("task",data.get("task"));
            model.addAttribute("counter",data.get("counter"));
            model.addAttribute("logs", MarkUtils.getSixLatestLogs(globalVar.getSyncLogs()));
            return new  ModelAndView("index","userModel",model);
        }else {
            List<User> users = globalVar.getUsers();
            if (users != null){
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getUserName().equals(userName) && users.get(i).getPassword().equals(password)){
                        log.info("用户{}访问个人主页成功", userName);
                        Map<String, Integer> data = tableService.getDataForIndex();
                        model.addAttribute("users",data.get("user"));
                        model.addAttribute("table",data.get("table"));
                        model.addAttribute("task",data.get("task"));
                        model.addAttribute("counter",data.get("counter"));
                        model.addAttribute("data",tableService.getDataForIndex());
                        model.addAttribute("logs", MarkUtils.getSixLatestLogs(globalVar.getSyncLogs()));
                        return new  ModelAndView("index","userModel",model);
                    }
                }
            }
            log.warn("用户{}访问个人主页失败", userName);
            model.addAttribute("msg","用户认证失败");
            return new ModelAndView("login","userModel",model);
        }
    }

    /**
     * 用户登录接口
     * @param user
     * @return
     */
    @PostMapping("/login")
    public @ResponseBody
    Result handleLogin(@RequestBody User user){

        if ("".equals(user.getUserName()) || user.getUserName() == null){
            return ResultUtils.error(20,"用户名、密码不能为空");
        }

        if (user.getUserName().equals(globalVar.getAdminName()) && user.getPassword().equals(globalVar.getAdminPassword())){
            //keyGen用来保证每次登陆token不一样，因为用户不用经常登录，不设置过期时间
            Double keyGen = Math.random();
            String token = MarkUtils.initToken(user,keyGen.toString());
            Map map = new HashMap();
            map.put("access_token",token);
            map.put("user",user.getUserName());
            return ResultUtils.success(map,"登陆成功");

        }else {
            List<User> users = globalVar.getUsers();
            String userName = user.getUserName();
            String password = user.getPassword();
            if (users != null){
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getUserName().equals(userName) && users.get(i).getPassword().equals(password)){
                        log.info("用户{}访问个人主页成功", userName);
                        Double keyGen = Math.random();
                        String token = MarkUtils.initToken(user,keyGen.toString());
                        Map map = new HashMap();
                        map.put("access_token",token);
                        map.put("user",user.getUserName());
                        return ResultUtils.success(map,"登陆成功");
                    }
                }
            }
            return ResultUtils.error(10,"用户名或密码错误");
        }
    }

    @GetMapping("/login")
    public ModelAndView login(Model model){
        return new ModelAndView("login");
    }

    @PostMapping("/register")
    @ResponseBody
    public Result handleRegister(@RequestBody User user){
        if (StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword())){
            return ResultUtils.error(10,"用户名或密码为空");
        }

        if (user.getUserName().equals(globalVar.getAdminName())){
            return ResultUtils.error(9,"用户名已经存在");
        }else{
            // 检查用户是否存在
            List<User> users = globalVar.getUsers();
            if (users == null){
                globalVar.setUsers(new LinkedList<User>());
                users = globalVar.getUsers();
            }
            if (users.contains(user.getUserName())){
                return ResultUtils.error(30,"用户名已存在, 请重新配置一个.");
            }
            //keyGen用来保证每次登陆token不一样，因为用户不用经常登录，不设置过期时间
            Double keyGen = Math.random();
            String token = MarkUtils.initToken(user,keyGen.toString());
            Map map = new HashMap();
            map.put("access_token",token);
            users.add(user);
            return ResultUtils.success(map,"注册成功");

        }
    }

    @GetMapping("/register")
    public ModelAndView register(){
        return new ModelAndView("register");
    }


    @GetMapping("/forms")
    public ModelAndView forms(Model model, HttpServletRequest request){
        String token = request.getParameter("token");
        String userName = MarkUtils.getUserNameByToken(token);
        String password = MarkUtils.getUserPasswordByToken(token);
        model.addAttribute("user",userName);
        if (globalVar.getAdminName().equals(userName) && globalVar.getAdminPassword().equals(password)){
            log.info("用户{}访问数据源配置界面成功", userName);
            model.addAttribute("username",globalVar.getUsername());
            model.addAttribute("password",globalVar.getPassword());
            model.addAttribute("DBType",globalVar.getDBType());
            model.addAttribute("url",globalVar.getDbUrl());
            return new  ModelAndView("forms","userModel",model);
        }else {
            List<User> users = globalVar.getUsers();
            if (users != null){
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getUserName().equals(userName) && users.get(i).getPassword().equals(password)){
                        log.info("用户{}访问数据源配置界面成功", userName);
                        model.addAttribute("username",globalVar.getUsername());
                        model.addAttribute("password",globalVar.getPassword());
                        model.addAttribute("DBType", globalVar.getDBType()).addAttribute("url", globalVar.getDbUrl());
                        return new  ModelAndView("forms","userModel",model);
                    }
                }
            }
            log.warn("用户{}访问个人主页失败", userName);
            model.addAttribute("msg","用户认证失败");
            return new ModelAndView("login","userModel",model);
        }
    }


    @GetMapping("/account")
    public ModelAndView account(Model model, HttpServletRequest request){
        String token = request.getParameter("token");
        String userName = MarkUtils.getUserNameByToken(token);
        String password = MarkUtils.getUserPasswordByToken(token);
        model.addAttribute("user",userName);
        if (globalVar.getAdminName().equals(userName) && globalVar.getAdminPassword().equals(password)){
            log.info("用户{}访问账号修改界面成功", userName);
            return new  ModelAndView("account","userModel",model);
        }else {
            List<User> users = globalVar.getUsers();
            if (users != null){
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getUserName().equals(userName) && users.get(i).getPassword().equals(password)){
                        log.info("用户{}访问账号修改界面成功", userName);
                        return new  ModelAndView("account","userModel",model);
                    }
                }
            }
            log.warn("用户{}访问个人主页失败", userName);
            model.addAttribute("msg","用户认证失败");
            return new ModelAndView("login","userModel",model);
        }
    }

    @PostMapping("/changeAccount")
    public @ResponseBody Result changeAccount(@RequestBody User user){
        String token = user.getToken();
        String userName = MarkUtils.getUserNameByToken(token);
        String password = MarkUtils.getUserPasswordByToken(token);
        if (StringUtils.isEmpty(user.getPassword())){
            return ResultUtils.error(9,"密码不能为空");
        }
        if (password.equals(user.getPassword())){
            return ResultUtils.error(3,"和老密码一样");
        }
        // admin
        if (userName.equals(globalVar.getAdminName())){
            globalVar.setAdminPassword(user.getPassword());
            return ResultUtils.success();
        }else {
            List<User> users = globalVar.getUsers();
            if (users == null || users.size() == 0){
                return ResultUtils.error(6,"当前操作非法，用户身份出现问题，请重新登录");
            }
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getUserName().equals(userName)){
                    users.get(i).setPassword(user.getPassword());
                    users.get(i).setEmail(user.getEmail());
                    return ResultUtils.success();
                }
            }
            return ResultUtils.error(6,"当前用户非法状态，token不一致，请重新登录");
        }
    }

    @GetMapping("/charts")
    public ModelAndView charts(Model model, HttpServletRequest request){
        // getAllSyncRequests
        String token = request.getParameter("token");
        String userName = MarkUtils.getUserNameByToken(token);
        String password = MarkUtils.getUserPasswordByToken(token);
        model.addAttribute("user",userName);
        if (globalVar.getAdminName().equals(userName) && globalVar.getAdminPassword().equals(password)){
            log.info("用户{}访问数据同步状态界面成功", userName);
            model.addAttribute("states", tableService.getAllSyncRequest());
            return new  ModelAndView("charts","userModel",model);
        }else {
            List<User> users = globalVar.getUsers();
            if (users != null){
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getUserName().equals(userName) && users.get(i).getPassword().equals(password)){
                        log.info("用户{}访问数据同步状态界面成功", userName);
                        model.addAttribute("states", tableService.getAllSyncRequest());
                        return new  ModelAndView("charts","userModel",model);
                    }
                }
            }
            log.warn("用户{}访问个人主页失败", userName);
            model.addAttribute("msg","用户认证失败");
            return new ModelAndView("login","userModel",model);
        }
    }

    @GetMapping("/tables")
    public ModelAndView tables(Model model, HttpServletRequest request){
        String token = request.getParameter("token");
        String userName = MarkUtils.getUserNameByToken(token);
        String password = MarkUtils.getUserPasswordByToken(token);
        model.addAttribute("user",userName);
        model.addAttribute("tables",globalVar.getDBStatus());
        if (globalVar.getAdminName().equals(userName) && globalVar.getAdminPassword().equals(password)){
            log.info("用户{}访问设置数据表同步界面成功", userName);
            return new  ModelAndView("tables","userModel",model);
        }else {
            List<User> users = globalVar.getUsers();
            if (users != null){
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getUserName().equals(userName) && users.get(i).getPassword().equals(password)){
                        log.info("用户{}访问设置数据表同步界面成功", userName);
                        return new  ModelAndView("tables","userModel",model);
                    }
                }
            }
            log.warn("用户{}访问个人主页失败", userName);
            model.addAttribute("msg","用户认证失败");
            return new ModelAndView("login","userModel",model);
        }
    }

}
