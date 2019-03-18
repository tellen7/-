package com.laowang.config;

import com.laowang.application.Application;
import com.laowang.common.GlobalVar;
import com.laowang.model.TableStatus;
import com.laowang.service.TableService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class ScheduleCompressTest {

    @Autowired
    ScheduleTask scheduleTask;
    @Autowired
    GlobalVar globalVar;
    @Autowired
    TableService service;

    @Test
    public void syncTotalColumn() {
        service.getInitDBAllTableStatus();
        scheduleTask.syncTotalColumn();
        List<TableStatus> statuses = globalVar.getDBStatus();
        if (statuses == null ){
            return;
        }
        for (int i = 0; i < statuses.size(); i++) {
            TableStatus table = statuses.get(i);
            log.info("table.name:{}, table.total_column:{}",table.getTableName(),table.getTotalColumn());
        }
        scheduleTask.scheduledCompress();
    }
}