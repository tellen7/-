package com.laowang.start;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.laowang.model.ControlBean;
import com.laowang.model.DownloadResult;
import com.laowang.model.GlobalVar;
import com.laowang.tasks.Downloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author wangyonghao
 */
@Slf4j
@Component
@ConditionalOnBean(GlobalVar.class)
public class ScheduledDataDownloadPool extends Thread implements CommandLineRunner {

    @Autowired
    GlobalVar globalVar;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    Downloader downloader;

    private final static ScheduledThreadPoolExecutor SCHEDULER = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("dataDownloadPool-%d").setDaemon(true).build());

    @Override
    public void run(String... args) throws Exception {
        this.start();
    }

    @Override
    public void run() {
        //0.开启定时下载功能
        if (globalVar.isOpenDownload()) {
            SCHEDULER.scheduleWithFixedDelay(() -> {
                try {
                    //1.拉取待下载文件的list
                    Map<String, Integer> map = getList();
                    //2.过滤出待下载的文件去下载
                    if (map != null) {
                        if (map.size() == 0){
                            log.info("目前没有文件待下载");
                        }
                        map.forEach((key, value) -> {
                            if (!ControlBean.filesTotal.containsKey(key)) {
                                //2.1下载过的文件在列表中不存在
                                log.info("===> start download:{} length:{}", key, value);
                                //2.2创建下载任务
                                String msg = downloader.download(key, value);
                                //2.3创建下载任务结果日志
                                if ("ok".equals(msg)) {
                                    log.info("===>创建下载{}任务完成", key);
                                } else {
                                    log.error("===>创建下载{}任务失败; {}", key, msg);
                                }
                            } else {
                                //2.1待下载文件已经被下载过了，日志记录
                                log.info("===>文件{}正在下载中", key);
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("===>下载文件线程池异常");
                }
                //20秒拉取一次待下载文件
            }, 10, 20, TimeUnit.SECONDS);
        } else {
            log.info("===>没有开启下载功能");
        }
    }

    /**
     * 拉取待下载文件的文件列表
     * @return Map<String   ,   Integer>（服务器文件名，文件长度）
     */
    public Map<String, Integer> getList() {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<String, Object>();
        //设置token
        headers.add("Authorization",globalVar.getToken());
        param.add("name",globalVar.getAdminName());
        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(param, headers);
        ResponseEntity<DownloadResult> response = null;
        try {
            //可能会出现请求异常
            response = restTemplate.exchange(globalVar.getDataListUrl(), HttpMethod.POST, httpEntity, DownloadResult.class);
            //下载成功,反序列化，判断是否下载成功
            DownloadResult result = response.getBody();
            if (result.getCode() == DownloadResult.StatusCode.SUCCESS) {
                return (Map<String, Integer>) result.getData();
            } else {
                log.error("===>获取待下载文件列表失败...");
                return null;
            }
        } catch (RestClientException e) {
            log.info("===>网络异常，获取待下载文件失败...", e);
            return null;
        }
    }
}
