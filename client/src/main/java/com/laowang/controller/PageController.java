package com.laowang.controller;

import com.laowang.model.GlobalVar;
import com.laowang.service.TableService;
import com.laowang.utils.MarkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author wangy
 */
@Controller
@Slf4j
public class PageController {

    @Autowired
    private TableService service;
    @Autowired
    private GlobalVar globalVar;

    @GetMapping("/login")
    public ModelAndView login(Model model) {
        return new ModelAndView("login");
    }

    @GetMapping("/register")
    public ModelAndView register(Model model) {
        return new ModelAndView("register");
    }

    @GetMapping("/index")
    public ModelAndView index(Model model) {
        Map map = service.getUserSyncState();
        if (map != null) {
            model.addAttribute("sync", map.get("sync"));
            model.addAttribute("task", map.get("task"));
            model.addAttribute("approval", map.get("approval"));
            model.addAttribute("total", map.get("total"));
            model.addAttribute("user", globalVar.getAdminName());
            model.addAttribute("syncLogs", MarkUtils.getSixLatestLogs(globalVar.getSyncLogs()));
        }
        return new ModelAndView("index");
    }

    @GetMapping("/account")
    public ModelAndView account(Model model) {
        model.addAttribute("user", globalVar.getAdminName());
        return new ModelAndView("account");
    }

    @GetMapping("/forms")
    public ModelAndView forms(Model model) {
        model.addAttribute("user", globalVar.getAdminName());
        model.addAttribute("username",globalVar.getUsername());
        model.addAttribute("password",globalVar.getPassword());
        model.addAttribute("DBType",globalVar.getDBType());
        model.addAttribute("url",globalVar.getDbUrl());
        return new ModelAndView("forms");
    }

    @GetMapping("/tables")
    public ModelAndView tables(Model model) {
        model.addAttribute("user", globalVar.getAdminName());
        model.addAttribute("data", service.getUserTable());
        return new ModelAndView("tables");
    }

}
