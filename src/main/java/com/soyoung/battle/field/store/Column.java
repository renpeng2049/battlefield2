package com.soyoung.battle.field.store;

public class Column<T> {

    private Integer offset;
    private String name;

    private Class<T> clazz;

    private Integer length;

    private T value;

    public Column(String name,Class<T> clazz ,Integer length,Integer offset){
        this.offset = offset;
        this.name = name;
        this.clazz = clazz;
        if(null == length){
            this.length = getPrimitiveLen(clazz);
        } else {
            this.length = length;
        }
        if(this.length <= 0){
            throw new IllegalArgumentException("table column "+ this.name +" is illegal");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Integer getOffset() {
        return offset;
    }

    public int getPrimitiveLen(Class clazz){

        if(clazz.equals(byte.class)){
            return 1;
        }else if(clazz.equals(char.class)){
            return 2;
        }else if(clazz.equals(Integer.class)){
            return 4;
        }else if(clazz.equals(Long.class)){
            return 8;
        }else if(clazz.equals(Float.class)){
            return 4;
        }else if(clazz.equals(Double.class)){
            return 8;
        }else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Column{" +
                "offset=" + offset +
                ", name='" + name + '\'' +
                ", clazz=" + clazz +
                ", length=" + length +
                ", value=" + value +
                '}';
    }
}
