package com.laowang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncTable {
    private String tableName;
    private String state;
    private List<List<String>> sample;

}
