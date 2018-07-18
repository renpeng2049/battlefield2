package com.soyoung.battle.field.store;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PriTypeEnum {

    BYTE(1,1, "byte"),
    SHORT(2,2, "short"),
    INTEGER(3,4,"int"),
    LONG(4,8,"long"),
    FLOAT(5,4,"float"),
    DOUBLE(6,8,"double"),

    ;


    private int index;
    private int length;
    private String desc;

    private PriTypeEnum(int index,int length, String desc)
    {
        this.index = index;
        this.length = length;
        this.desc = desc;
    }

    public int getIndex()
    {
        return this.index;
    }

    public int getLength() {
        return length;
    }

    public String getDesc()
    {
        return desc;
    }

    private static final Map<Integer, PriTypeEnum> lookup = new HashMap<Integer, PriTypeEnum>();
    static
    {
        for (PriTypeEnum s : EnumSet.allOf(PriTypeEnum.class))
            lookup.put(s.getIndex(), s);
    }

    public static PriTypeEnum get(int index)
    {
        return lookup.get(index);
    }
}
