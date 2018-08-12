package com.soyoung.battle.field.btree;

import com.soyoung.battle.field.store.Cursor;
import com.soyoung.battle.field.store.Page;

import java.nio.ByteBuffer;

public class LeafNode extends TreeNode {

    //数据的id(可以是行号)单独存储
    public static final Integer CELL_KEY_SIZE = 4;
    //cellNum字段占用的字节数
    private static final Integer CELL_NUM_SIZE = 4;
    public static final Integer LEAF_NODE_HEADER_SIZE = COMMON_HEADER_SIZE + CELL_NUM_SIZE;

    //节点包含的数据条数
    private Integer cellNum;

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
        Integer bufferPosition = page.getPageBuffer().position();
        if(searchPosition >= bufferPosition){
            return null;
        } else {
            return page.getPageBuffer().getInt();
        }
    }

    //根据数据序号查询数据id
    public Integer getCellKeyByPosition(Integer position){
        return page.getPageBuffer().getInt(LEAF_NODE_HEADER_SIZE + position);
    }

    public void getData(byte[] dst, Integer position,int length){
        page.getPageBuffer().mark();
        page.getPageBuffer().position(position);
        page.getPageBuffer().get(dst,0,length);
        page.getPageBuffer().reset();
    }

    public void putData(byte[] src,Integer position,int length){
        page.getPageBuffer().position(position);
        page.getPageBuffer().put(src,0,length);
    }

    public void putKeyAndValue(Cursor cursor,Integer key,ByteBuffer value){

        value.flip(); //翻转buffer
//        Integer position = LEAF_NODE_HEADER_SIZE + cursor.getCellNo() * (cursor.getTable().getRowSize() + CELL_KEY_SIZE);
//        page.getPageBuffer().mark();
//        page.getPageBuffer().position(position);
//        page.getPageBuffer().putInt(key);
//        page.getPageBuffer().put(value);
//        page.getPageBuffer().reset();

        page.getPageBuffer().putInt(key);
        page.getPageBuffer().put(value);
    }

    public Integer getBufferPostion(){

        return page.getPageBuffer().position();
    }

    public void setBufferPostion(Integer postion){
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
