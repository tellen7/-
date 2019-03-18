package com.laowang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBSource {
    private String DBType;
    private String url;
    private String username;
    private String password;
    private String driverName;
    private String validationQueryString;
}
