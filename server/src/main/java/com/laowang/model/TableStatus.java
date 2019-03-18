package com.laowang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author wangy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableStatus {
    /**是否时同步的表*/
    private String sync;
    /**表schema名*/
    private String tableSchema;
    /**表名*/
    private String tableName;
    /**表总行数*/
    private int totalColumn;
    /**表已经压缩行数*/
    private int compressColumn;
    /**最后压缩时间*/
    private String lastCompressTime;
    /**压缩文件位置（文件名.时间.zip）*/
    private String filePath;
    /**同步该表的用户*/
    private List<String> users;
    /**标记每个用户同步状态*/
    private Map<String,Integer> userSyncState;

    public void init(){
        this.sync = "0";
        this.compressColumn = 0;
        this.totalColumn = 0;
        this.lastCompressTime = "";
        this.users = null;
        this.userSyncState = null;
    }
}
