package com.soyoung.battle.field.btree;

import com.soyoung.battle.field.store.Page;

public abstract class TreeNode {

    /**
     * 节点头信息,公共包含如下
     * 1. 节点类型 byte
     * 2. 是否根节点 byte
     * 3. 父节点指针(页码) Integer
     */
    public static final Integer COMMON_HEADER_SIZE = 1 + 1 + 4;
    protected NodeTypeEnum nodeTypeEnum;
    //是否根节点，0否1是
    protected boolean isRoot;
    //父节点
    protected Integer parent;
    protected Page page;

    public TreeNode(Page page,NodeTypeEnum nodeTypeEnum,boolean isRoot,Integer parent){
        this.page = page;
        this.nodeTypeEnum = nodeTypeEnum;
        this.isRoot = isRoot; //是否根节点，0否1是
        this.parent = parent;
    }

    public NodeTypeEnum getNodeTypeEnum() {
        return nodeTypeEnum;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public Integer getParent() {
        return parent;
    }

    public Page getPage() {
        return page;
    }
}
