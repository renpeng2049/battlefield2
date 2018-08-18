package com.soyoung.battle.field.btree;

import com.soyoung.battle.field.store.Cursor;
import com.soyoung.battle.field.store.Page;
import com.soyoung.battle.field.store.Table;

import java.nio.ByteBuffer;

public class LeafNode extends TreeNode {

    //数据的id(可以是行号)单独存储
    public static final Integer CELL_KEY_SIZE = 4;
    //cellNum字段占用的字节数
    private static final Integer CELL_NUM_SIZE = 4;
    public static final Integer LEAF_NODE_HEADER_SIZE = COMMON_HEADER_SIZE + CELL_NUM_SIZE;

    public LeafNode(Page page){
        super(page);
    }

    //从文件中获取数据条数
    public Integer getCellNum() {

        return page.getPageBuffer().getInt(COMMON_HEADER_SIZE);
    }

    public void incrCellNum(){
        Integer cellNum = page.getPageBuffer().getInt(COMMON_HEADER_SIZE);
        page.getPageBuffer().position(COMMON_HEADER_SIZE);
        page.getPageBuffer().putInt(cellNum+1);
    }

    //根据cursor查询数据id
    public Integer getCellKey(Cursor cursor){
        Integer searchPosition = LEAF_NODE_HEADER_SIZE + cursor.getCellNo() * (cursor.getTable().getRowSize() + CELL_KEY_SIZE); // 数据kv形式存储，key占4字节
        if(searchPosition >= getEndPostion(cursor.getTable())){
            return null;
        } else {
            return page.getPageBuffer().getInt(searchPosition);
        }
    }

    //根据数据序号查询数据id
    public Integer getCellKeyByPosition(Integer position){
        return page.getPageBuffer().getInt(LEAF_NODE_HEADER_SIZE + position);
    }

    public void makeSpace(Cursor cursor){

        Integer behindDataLen = (getCellNum() - cursor.getCellNo())*(CELL_KEY_SIZE + cursor.getTable().getRowSize());

        byte[] dst = new byte[behindDataLen];
        Integer relativePostion = LeafNode.LEAF_NODE_HEADER_SIZE + cursor.getCellNo() * (CELL_KEY_SIZE + cursor.getTable().getRowSize());
        page.getPageBuffer().position(relativePostion);
        //获取游标后面的数据
        page.getPageBuffer().get(dst,0,behindDataLen);

        //put数据到后一位空间
        page.getPageBuffer().position(relativePostion + (CELL_KEY_SIZE + cursor.getTable().getRowSize()));
        page.getPageBuffer().put(dst,0,behindDataLen);
    }


    public void putKeyAndValue(Cursor cursor,Integer key,ByteBuffer value){

        Integer cellNum = page.getPageBuffer().getInt(COMMON_HEADER_SIZE);
        page.getPageBuffer().position(COMMON_HEADER_SIZE);
        page.getPageBuffer().putInt(cellNum+1);

        Integer relativePostion = LeafNode.LEAF_NODE_HEADER_SIZE + cursor.getCellNo() * (CELL_KEY_SIZE + cursor.getTable().getRowSize());
        page.getPageBuffer().position(relativePostion);

        page.getPageBuffer().putInt(key);
        value.flip(); //翻转buffer
        page.getPageBuffer().put(value);

        page.getPageBuffer().position(getEndPostion(cursor.getTable()));
    }

    public void resetPage(Table table){

        page.getPageBuffer().limit(Page.PAGE_SIZE);
        page.getPageBuffer().position(getEndPostion(table));
    }

    public Integer getEndPostion(Table table){

        return LEAF_NODE_HEADER_SIZE + getCellNum() * (CELL_KEY_SIZE + table.getRowSize());
    }

    public void setEndPostion(Integer postion){
        page.getPageBuffer().position(postion);
    }

    public void setBufferLimit(Integer limit){
        page.getPageBuffer().limit(limit);
    }

    //叶子节点中留给数据的空间. PAGE_SIZE - COMMON_HEADER_SIZE - CELL_NUM_SIZE
    public static Integer getSpaceForCell(){
        return Page.PAGE_SIZE - LEAF_NODE_HEADER_SIZE;
    }
}
