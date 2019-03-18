package com.laowang.model;

import lombok.Data;

@Data
public class SyncRequestMessage {


    private String tableName;
    private String user;
    private int code;
    private String sync;
}
