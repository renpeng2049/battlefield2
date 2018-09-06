package com.soyoung.battle.field.btree;

import com.soyoung.battle.field.store.Page;

public class InternalNode extends TreeNode {


    public InternalNode(Page page,NodeTypeEnum nodeTypeEnum,boolean isRoot, Integer parent){
        super(page,nodeTypeEnum,isRoot,parent);
    }
}
