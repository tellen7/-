package com.laowang.start;

import com.alibaba.fastjson.JSONObject;
import com.laowang.model.DownloadResult;
import com.laowang.model.GlobalVar;
import com.laowang.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author wangyonghao
 */
@Slf4j
@Component
@ConditionalOnBean(GlobalVar.class)
public class RefreshToken implements CommandLineRunner {

    @Autowired
    private GlobalVar globalVar;

    @Autowired
    RestTemplate restTemplate;

    @Override
    public void run(String... strings) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> mediaTypes = new LinkedList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);

        JSONObject postData = new JSONObject();
        postData.put("userName",globalVar.getAdminName());
        postData.put("password",globalVar.getAdminPassword());

        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(postData, headers);
        ResponseEntity<Result> response = null;
        try {
            //可能会出现请求异常
            response = restTemplate.postForEntity(globalVar.getLogin(), httpEntity, Result.class);
            //下载成功,反序列化，判断是否下载成功
            Result result = response.getBody();
            if (result.getCode() == 0) {
                Map data = (Map) result.getData();
                globalVar.setToken(data.get("access_token").toString());
                log.info("获取登录token成功, {}", globalVar.getToken());
            } else {
                response = restTemplate.postForEntity(globalVar.getRegister(), httpEntity, Result.class);
                //下载成功,反序列化，判断是否下载成功
                result = response.getBody();
                if (result.getCode() == 0){
                    Map data = (Map) result.getData();
                    globalVar.setToken(data.get("access_token").toString());
                    log.info("获取注册token成功, {}", globalVar.getToken());
                    return;
                }
                log.error("===>获取token失败, {}", result.getMsg());
            }
        } catch (RestClientException e) {
            log.info("===>网络异常，获取token失败...", e);
        }
    }

    public void refresh(){
        try {
            this.run();
        } catch (Exception e) {
            log.error("refresh token occur a exception ");
        }
    }
}
