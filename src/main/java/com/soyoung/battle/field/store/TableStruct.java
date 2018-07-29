package com.soyoung.battle.field.store;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class TableStruct {

    private List<Column> columnList;

    public TableStruct(){
        columnList = Lists.newArrayList();

    }

    public void addColumn(Column column){

        columnList.add(column);
    }

    public List<Column> getColumnList(){

        return this.columnList;
    }

    public int getRowSize(){

        return getColumnList().stream().mapToInt(Column::getLength).sum();
    }

}
