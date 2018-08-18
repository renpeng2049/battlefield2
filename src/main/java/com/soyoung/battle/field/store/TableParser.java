package com.soyoung.battle.field.store;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class TableParser {


    public Row parse2Row(Table table,JSONObject json){

        List<Column> columnList = table.getColumnList();
        for(Column column : columnList){

            fillColumnValue(column,json);
        }

        Row row = new Row();
        row.setColumnList(columnList);
        row.setKey(json.getInteger("id"));

        return row;
    }

    public ByteBuffer getDataFromRow(Row row){

        ByteBuffer byteBuffer = ByteBuffer.allocate(row.getRowSize());
        List<Column> columnList = row.getColumnList();

        for(Column column : columnList){

            putColumn2Buffer(byteBuffer,column);
        }

        return byteBuffer;
    }


    public List<Column> getColumnsFromBuffer(List<Column> columnList, ByteBuffer buffer){

        List<Column> newColumnList = Lists.newArrayList();
        for(Column column : columnList){

            Column newColumn = getColumnFromBuffer(buffer,column);
            newColumnList.add(newColumn);
        }

        return newColumnList;
    }

    public Row getRowFromBuffer(List<Column> columnList, ByteBuffer buffer){

        Row row = new Row();
        row.setKey(buffer.getInt());

        List<Column> newColumnList = Lists.newArrayList();
        for(Column column : columnList){

            Column newColumn = getColumnFromBuffer(buffer,column);
            newColumnList.add(newColumn);
        }

        row.setColumnList(newColumnList);
        return row;
    }

    private void putColumn2Buffer(ByteBuffer buffer ,Column column){

        Class clazz = column.getClazz();
        int length = column.getLength();
        Object value = column.getValue();

        //如果值为空，则不设值仅移动position
        if(null == value){
            buffer.position(column.getOffset() + length);
            return;
        }

        if(clazz.equals(byte.class)){
            buffer.put((byte)value);
        } else if(clazz.equals(char.class)){
            buffer.putChar((char)value);
        } else if(clazz.equals(Integer.class)){
            buffer.putInt((Integer)value);
        } else if(clazz.equals(Long.class)){
            buffer.putLong((Long)value);
        } else if(clazz.equals(Float.class)){
            buffer.putFloat((Float)value);
        } else if(clazz.equals(Double.class)){
            buffer.putDouble((Double)value);
        } else if(clazz.equals(String.class)){
            byte[] valueBytes = ((String)value).getBytes();

            byte[] columnBytes = new byte[length];
            System.arraycopy(valueBytes,0,columnBytes,0,valueBytes.length);
            buffer.put(columnBytes,0,columnBytes.length);
        } else {

        }
    }

    private Column getColumnFromBuffer(ByteBuffer buffer ,Column column){


        Class clazz = column.getClazz();
        int length = column.getLength();

        Column newColumn = new Column(column.getName(),clazz,length,column.getOffset());

        if(clazz.equals(byte.class)){
            newColumn.setValue(buffer.get());
        } else if(clazz.equals(char.class)){
            newColumn.setValue(buffer.getChar());
        } else if(clazz.equals(Integer.class)){
            newColumn.setValue(buffer.getInt());
        } else if(clazz.equals(Long.class)){
            newColumn.setValue(buffer.getLong());
        } else if(clazz.equals(Float.class)){
            newColumn.setValue(buffer.getFloat());
        } else if(clazz.equals(Double.class)){
            newColumn.setValue(buffer.getDouble());
        } else if(clazz.equals(String.class)){
            byte[] valueBytes = new byte[length];
            buffer.get(valueBytes,0,valueBytes.length);
            newColumn.setValue(new String(valueBytes));
        } else {

        }
        return newColumn;
    }

    public Column fillColumnValue(Column column,JSONObject json){

        String name = column.getName();
        Class clazz = column.getClazz();

        if(clazz.equals(byte.class)){
            column.setValue(json.getByteValue(name));
        } else if(clazz.equals(char.class)){
            String c = json.getString(name);
            column.setValue(c.charAt(0));
        } else if(clazz.equals(Integer.class)){
            column.setValue(json.getInteger(name));
        } else if(clazz.equals(Long.class)){
            column.setValue(json.getLong(name));
        } else if(clazz.equals(Float.class)){
            column.setValue(json.getFloat(name));
        } else if(clazz.equals(Double.class)){
            column.setValue(json.getDouble(name));
        } else if(clazz.equals(String.class)){
            column.setValue(json.getString(name));
        } else {

        }
        return column;
    }


}
