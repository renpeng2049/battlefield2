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
    protected boolean isRoot;
    protected TreeNode parent;
    protected Page page;

    public TreeNode(Page page){
        this.page = page;
    }

    public NodeTypeEnum getNodeTypeEnum() {
        return nodeTypeEnum;
    }

    public void setNodeTypeEnum(NodeTypeEnum nodeTypeEnum) {
        this.nodeTypeEnum = nodeTypeEnum;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public Page getPage() {
        return page;
    }
}
