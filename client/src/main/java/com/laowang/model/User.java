package com.laowang.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class User {
    private String userName;
    private String password;
    private String tableName;
    private String email;
    private int code;
    private String token;
}