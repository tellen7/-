package com.laowang.application;

import com.laowang.common.GlobalVar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * @author wangyonghao
 */
@Slf4j
@Component
@ConditionalOnBean(GlobalVar.class)
public class InitAplication implements CommandLineRunner {

    @Autowired
    private GlobalVar globalVar;

    @Override
    public void run(String... strings) throws Exception {
        globalVar.setSyncCounter(0);
        globalVar.setSyncLogs(new LinkedList<>());
    }
}
