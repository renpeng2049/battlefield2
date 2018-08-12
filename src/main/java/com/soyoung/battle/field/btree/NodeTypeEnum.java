package com.soyoung.battle.field.btree;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * node类型
 */
public enum NodeTypeEnum {

    NODE_INTERNAL(0,"非叶子节点"),
    NODE_LEAF(1,"叶子节点"),
    ;

    private int index;
    private String desc;

    private NodeTypeEnum(int index, String desc)
    {
        this.index = index;
        this.desc = desc;
    }

    public int getIndex()
    {
        return this.index;
    }

    public String getDesc()
    {
        return desc;
    }

    private static final Map<Integer, NodeTypeEnum> lookup = new HashMap<Integer, NodeTypeEnum>();

    static
    {
        for (NodeTypeEnum s : EnumSet.allOf(NodeTypeEnum.class))
            lookup.put(s.getIndex(), s);
    }

    public static NodeTypeEnum get(int index)
    {
        return lookup.get(index);
    }
}
