package com.laowang.service;

import com.laowang.start.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TableServiceTest {

    @Autowired
    private TableService service;

    @Test
    public void getUserTable() {
        service.getUserTable();
    }

    @Test
    public void getUserSyncState(){
        service.getUserSyncState();
    }
}