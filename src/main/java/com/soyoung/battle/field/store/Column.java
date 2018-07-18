package com.soyoung.battle.field.store;

public class Column {

    private String name;

    private Object priType;

    private Integer length;

    private Object value;


    public Column(String name,Object priType ,Integer length){
        this.name = name;
        this.priType = priType;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getPriType() {
        return priType;
    }

    public void setPriType(Object priType) {
        this.priType = priType;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
