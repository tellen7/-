package com.laowang.service;

import com.laowang.application.Application;
import com.laowang.model.DBSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TableServiceTest {

    @Autowired
    TableService service;

    @Test
    public void changeJDBCDataSource() throws CloneNotSupportedException {
        DBSource source = new DBSource();
        source.setDriverName("com.mysql.jdbc.Driver");
        source.setDBType("mysql");
        source.setUrl("jdbc:mysql://127.0.0.1:3306/springboot?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        source.setUsername("root");
        source.setPassword("wyh000000");
        System.out.println(service.changeJDBCDataSource(source));
    }
}