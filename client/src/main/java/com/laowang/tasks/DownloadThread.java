package com.laowang.tasks;

import com.laowang.model.DownloadBlock;
import com.laowang.model.ControlBean;
import com.laowang.model.DownloadResult;
import com.laowang.model.GlobalVar;
import com.laowang.utils.MarkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

/**
 * @author wangyonghao
 */
@Slf4j
@Component
public class DownloadThread implements Runnable {

    @Autowired
    GlobalVar globalVar;

    @Override
    public void run() {
        while (true) {
            try {
                //下载文件块失败，停止下载
                if (ControlBean.continueDownload) {
                    DownloadBlock block = ControlBean.waitBlocks.take();
                    doDownload(block);
                }
            } catch (InterruptedException e) {
                log.error("===>下载内部异常{}", e);
            }

        }
    }

    public void doDownload(DownloadBlock block) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        ArrayList<MediaType> types = new ArrayList<MediaType>();
        types.add(MediaType.APPLICATION_OCTET_STREAM);
        headers.setAccept(types);

        headers.add("Authorization",globalVar.getToken());
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        //设置数据块相关
        param.add("filePath", String.valueOf(block.getServerFilePath()));
        param.add("startPosition", String.valueOf(block.getStart()));
        param.add("endPosition", String.valueOf(block.getEnd()));

        HttpEntity<MultiValueMap> httpEntity = new HttpEntity(param, headers);
        ResponseEntity<byte[]> response = null;

        try {
            log.info("===>开始下载{}【No.{}块文件块】", block.getServerFilePath(), block.getNumber());
            //可能会出现请求异常
            response = restTemplate.exchange(block.getUrl(), HttpMethod.POST, httpEntity, byte[].class);
            //下载成功,反序列化，判断是否下载成功
            byte[] bytes = response.getBody();
            DownloadResult result = (DownloadResult) MarkUtils.bytesToObject(bytes);
            if (result.getCode() == DownloadResult.StatusCode.SUCCESS) {
                //缓存下载内容
                block.setContent((byte[]) result.getData());
                ControlBean.successBlocks.add(block);
                log.info("===>成功下载{}【No.{}块文件块】", block.getServerFilePath(), block.getNumber());
                ControlBean.continueDownload = true;
            } else {
                //这里能收到response
                log.info("===>失败下载{}【No.{}块文件块】", block.getServerFilePath(), block.getNumber());
                //某个文件块下载失败次数到达3次，则需要丢弃文件对应的文件块以及删除本地temp文件
                block.getFailCount().addAndGet(1);
                if (block.getFailCount().get() > 3){
                    ControlBean.dropBlock.get(block.getServerFilePath()).addAndGet(1);
                }else {
                    ControlBean.loseBlocks.add(block);
                }
            }
        } catch (RestClientException e) {
            //这里对应 RestClientException 异常
            log.info("===>网络波动，失败下载{}【No.{}块文件块】{}", block.getServerFilePath(), block.getNumber(), e);
            ControlBean.loseBlocks.add(block);
            ControlBean.continueDownload = false;
        }

    }

}
