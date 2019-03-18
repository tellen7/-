package com.laowang.model;

import lombok.Data;

@Data
public class SyncLog {
    private String user;
    private String table;
    private String data;
    private String time;
}
