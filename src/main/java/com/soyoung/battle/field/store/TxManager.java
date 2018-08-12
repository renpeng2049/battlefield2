package com.soyoung.battle.field.store;

import com.soyoung.battle.field.btree.LeafNode;
import com.soyoung.battle.field.btree.TreeNode;
import com.soyoung.battle.field.common.logging.Loggers;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * transaction manager.
 */
public class TxManager {

    Logger logger = Loggers.getLogger(TxManager.class);
    private TableParser parser;

    public TxManager(TableParser parser){
        this.parser = parser;
    }

    public void insert(Table table,Row row){

        Integer key = row.getKey();
        if(null == key){
            throw new IllegalStateException("id must not null");
        }
        if(key <= 0){
            throw new IllegalStateException("id must be positive");
        }

        //TODO 暂定根节点
        TreeNode rootNode = table.getRootNode();
        LeafNode leafNode = ((LeafNode)rootNode);
        Integer cellNum = leafNode.getCellNum();
        logger.info("cell num:{}",cellNum);
        Cursor cursor = new Cursor(table,0,0);


        // 如果页(node)数据已达上限
        if(cellNum >= table.getLeafNodeMaxRowNum()){
            throw new IllegalStateException("数据已达上限，当前节点需分裂或升级");
        }

        cursor = binarySearch(table, key, leafNode, cellNum);
        logger.info("cursor:{}",cursor);
        Integer searchKey = leafNode.getCellKey(cursor);
        if(null != searchKey){

            if(searchKey == key){
                throw new IllegalStateException("DUPLICATE KEY");
            }

            if(cursor.getCellNo() < cellNum){
                //移动cellNo之后的数据
                Integer behindDataLen = (cellNum - cursor.getCellNo())*getKVSize(table);
                byte[] behindData = new byte[behindDataLen];
                Integer indexPostion = LeafNode.LEAF_NODE_HEADER_SIZE + cursor.getCellNo()*getKVSize(table);

                leafNode.getData(behindData,indexPostion,behindDataLen);

                Integer newPosition = indexPostion + getKVSize(table);
                leafNode.putData(behindData,newPosition,behindDataLen);
            }
        }

        //插入新数据
        ByteBuffer buffer = parser.getDataFromRow(row);
        logger.info("buffer:{}",buffer);
        leafNode.putKeyAndValue(cursor,key,buffer);

        logger.info("pageBuffer:{}",leafNode.getPage());

        Integer position = leafNode.getBufferPostion();
        //更新cellNum
        leafNode.incrCellNum();
        //复位 position
        leafNode.setBufferPostion(position);

        //保存并写入文件
        table.getPager().savePage(leafNode.getPage());

        //TODO 写入后position limit都已改变，需复位. 改为满了再写入？

        leafNode.setBufferPostion(position);
        leafNode.setBufferLimit(Page.PAGE_SIZE);
    }

    private Cursor binarySearch(Table table, Integer key, LeafNode leafNode, Integer cellNum) {

        Cursor cursor = new Cursor();
        cursor.setTable(table);
        cursor.setPageNo(0); //TODO 暂定为0

        //二分查找需要插入的位置(同时可以判断id是否已存在)
        //Binary search
        int minIndex = 0;
        int maxIndex = cellNum;
        while (maxIndex != minIndex){
            Integer index = (minIndex + maxIndex)/2;
            Integer keyAtIndex = leafNode.getCellKeyByPosition(getKVSize(table)*index);
            if(key == keyAtIndex){
                cursor.setCellNo(index);
                return cursor;
            }
            if(key < keyAtIndex){
                maxIndex = index;
            } else {
                minIndex = index + 1;
            }
        }

        cursor.setCellNo(minIndex);
        return cursor;
    }

    public List<Column> select(Table table, Integer key){

        if(null == key){
            throw new IllegalStateException("id must not null");
        }
        if(key <= 0){
            throw new IllegalStateException("id must be positive");
        }


        //TODO 暂定根节点
        TreeNode rootNode = table.getRootNode();
        LeafNode leafNode = ((LeafNode)rootNode);
        Integer cellNum = leafNode.getCellNum();

        Cursor cursor = binarySearch(table, key, leafNode, cellNum);
        Integer searchKey = leafNode.getCellKey(cursor);
        if(searchKey != key){
            throw new IllegalStateException("no data,id=" + key);
        }

        Page page = leafNode.getPage();
        ByteBuffer pageBuffer = page.getPageBuffer();

        pageBuffer.mark();
        pageBuffer.position(cursor.getCellNo() * getKVSize(table));

        List<Column> columnList = table.getColumnList();
        parser.getColumnsFromBuffer(columnList,pageBuffer);

        //恢复position
        pageBuffer.reset();

        return columnList;
    }

    private Integer getKVSize(Table table){

        return table.getRowSize() + LeafNode.CELL_KEY_SIZE;
    }
}
