package com.soyoung.battle.field.store;

import com.google.common.collect.Lists;

import java.util.List;

public class Row {

    private Integer key;
    private List<Column> columnList;


    public Row(){
        columnList = Lists.newArrayList();
    }

    public Row(List<Column> columnList){
        this.columnList = columnList;
    }

    public void addColumn(Column column){

        columnList.add(column);
    }

    public List<Column> getColumnList(){

        return this.columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public int getRowSize(){

        return this.columnList.stream().mapToInt(Column::getLength).sum();
    }
}