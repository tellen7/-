package com.laowang.tasks;

import com.laowang.model.ControlBean;
import com.laowang.model.DownloadBlock;
import com.laowang.model.GlobalVar;
import com.laowang.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.ws.soap.Addressing;

/**
 * @author wangyonghao
 */
@Slf4j
@Component
public class WriteThread implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                DownloadBlock block = ControlBean.successBlocks.take();
                doWrite(block);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件块写入成功，清空引用，判断写入文件块数是否达到目标数，达到则清空filesCurrent中对应文件的引用
     * 写入文件失败，I/O异常，则把文件块放回到successBlocks，等下一次再写
     *
     * @param block 文件块
     */
    public void doWrite(DownloadBlock block) {
        //如果写入文件成功
        if (FileUtils.storeToFile(block)) {
            log.info("===>成功写入{}【No.{}块文件块】", block.getStoreFilePath(), block.getNumber());
            int number = ControlBean.filesCurrent.get(block.getServerFilePath()).addAndGet(1);
            if (ControlBean.filesTotal.get(block.getServerFilePath()).get() == number) {
                FileUtils.rename(block.getStoreFilePath());
                log.info("====>文件{}", block.getServerFilePath() + "下载完成...");
            }
        } else {
            //可能不发生
            log.info("===>失败写入{}【No.{}块文件块】", block.getStoreFilePath(), block.getNumber());
            ControlBean.successBlocks.add(block);
        }
    }

}
