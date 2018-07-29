package com.soyoung.battle.field.store;

import com.alibaba.fastjson.JSONObject;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

public class TableStructParser {


    public ByteBuffer parse2ByteBuff(JSONObject json,TableStruct tableStruct){

        ByteBuffer byteBuffer = ByteBuffer.allocate(tableStruct.getRowSize());
        List<Column> columnList = tableStruct.getColumnList();


        List<Column> newColumnList = columnList.stream().map(o -> fillColumnValue(o,json)).collect(Collectors.toList());

        for(Column column : newColumnList){

            putColumn2Buffer(byteBuffer,column);
        }

        return byteBuffer;
    }

    public List<Column> getColumnFromBuffer(TableStruct tableStruct,ByteBuffer buffer){

        List<Column> columnList = tableStruct.getColumnList();

        for(Column column : columnList){

            getColumn(buffer,column);
        }

        return columnList;
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

    private Column getColumn(ByteBuffer buffer ,Column column){

        Class clazz = column.getClazz();
        int length = column.getLength();

        if(clazz.equals(byte.class)){
            column.setValue(buffer.get());
        } else if(clazz.equals(char.class)){
            column.setValue(buffer.getChar());
        } else if(clazz.equals(Integer.class)){
            column.setValue(buffer.getInt());
        } else if(clazz.equals(Long.class)){
            column.setValue(buffer.getLong());
        } else if(clazz.equals(Float.class)){
            column.setValue(buffer.getFloat());
        } else if(clazz.equals(Double.class)){
            column.setValue(buffer.getDouble());
        } else if(clazz.equals(String.class)){
            byte[] valueBytes = new byte[length];
            buffer.get(valueBytes,0,valueBytes.length);
            column.setValue(new String(valueBytes));
        } else {

        }
        return column;
    }

    private Column fillColumnValue(Column column,JSONObject json){

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
