package com.soyoung.battle.field.store;

import com.google.common.collect.Maps;

import java.util.Map;

public class StoreSchemas {


    private final Map<String,TableStruct> structMap = Maps.newConcurrentMap();


    public void registerTable(String tableName,TableStruct tableStruct){
        structMap.putIfAbsent(tableName,tableStruct);
    }

    public TableStruct getTableStruct(String tableName){
        return structMap.get(tableName);
    }

    public Map<String, TableStruct> getStructMap() {
        return structMap;
    }

    public void load(){

        //构建sample
        TableStruct tableStruct = new TableStruct();
        tableStruct.addColumn(new Column<Integer>("id",Integer.class,null,0));
        tableStruct.addColumn(new Column<String>("name",String.class,32,tableStruct.getRowSize()));
        tableStruct.addColumn(new Column<String>("email",String.class,32,tableStruct.getRowSize()));

        registerTable("sample",tableStruct);

        //TODO 从文件中load 自定义schema信息
    }
}
