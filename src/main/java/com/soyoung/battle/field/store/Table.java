package com.soyoung.battle.field.store;

import com.soyoung.battle.field.btree.LeafNode;
import com.soyoung.battle.field.btree.TreeNode;

import java.util.List;

public class Table {

    private String tableName;
    private List<Column> columnList;

    //每行的大小
    private Integer rowSize;

    //跟节点
    private TreeNode rootNode;

    private Pager pager;


    public Table(String tableName,Row row,TreeNode rootNode,Pager pager){
        this.tableName = tableName;
        this.columnList = row.getColumnList();
        this.rowSize = row.getRowSize();
        this.rootNode = rootNode;
        this.pager = pager;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


    public List<Column> getColumnList(){

        return this.columnList;
    }

    public int getRowSize(){

        return this.rowSize;
    }

    public Pager getPager() {
        return pager;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    //叶子节点能够存储的数据行数
    public Integer getLeafNodeMaxRowNum(){

        return LeafNode.getSpaceForCell()/this.rowSize;
    }

    @Override
    public String toString() {
        return "Table{" +
                "tableName='" + tableName + '\'' +
                ", rowSize=" + rowSize +
                '}';
    }
}
