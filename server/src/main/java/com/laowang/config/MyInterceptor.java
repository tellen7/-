package com.laowang.config;

import com.alibaba.fastjson.JSONObject;
import com.laowang.common.GlobalVar;
import com.laowang.common.MarkUtils;
import com.laowang.common.ResultUtils;
import com.laowang.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
public class MyInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private GlobalVar globalVar;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截操作
        log.info("ip:{}, uri:{}",request.getRemoteAddr(),request.getRequestURI());
        String token = request.getHeader("Authorization");
        if ("".equals(token) || token == null){
            log.warn("token 为空请求数据");
            Result result = ResultUtils.error(-1,"用户认证失败");
            String jsonObjectStr = JSONObject.toJSONString(result);
            returnJson(response,jsonObjectStr);
            return false;
        }
        String userName = MarkUtils.getUserNameByToken(token);
        String password = MarkUtils.getUserPasswordByToken(token);
        if (globalVar.getAdminName().equals(userName) && globalVar.getAdminPassword().equals(password)){
            return true;
        }else{
            Result result = ResultUtils.error(-1,"用户认证失败");
            String jsonObjectStr = JSONObject.toJSONString(result);
            returnJson(response,jsonObjectStr);
            return false;
        }
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception{
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
            log.error("response error",e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}
