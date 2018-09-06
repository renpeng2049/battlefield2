package com.soyoung.battle.field.store;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.soyoung.battle.field.btree.InternalNode;
import com.soyoung.battle.field.btree.LeafNode;
import com.soyoung.battle.field.btree.NodeTypeEnum;
import com.soyoung.battle.field.btree.TreeNode;
import com.soyoung.battle.field.common.logging.Loggers;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;

import static com.soyoung.battle.field.btree.LeafNode.CELL_KEY_SIZE;

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

        //查找定位
        Cursor cursor = binarySearch(table, key, leafNode, cellNum);
        logger.info("cursor:{}",cursor);
        Integer searchKey = leafNode.getCellKey(cursor);
        if(null != searchKey){

            if(searchKey == key){
                throw new IllegalStateException("DUPLICATE KEY");
            }
        }

        // 如果页(node)数据已达上限
        if(cellNum >= table.getLeafNodeMaxRowNum()){
            //throw new IllegalStateException("数据已达上限，当前节点需分裂或升级");
            //TODO 分裂升级
            leafNodeSplitAndInsert(cursor,leafNode,key,row);

        } else {

            if(cursor.getCellNo() < cellNum){
                //移动cellNo之后的数据
                leafNode.makeSpace(cursor);
                logger.info("移动数据后，buffer:{}",leafNode.getPage());
            }

            //插入新数据
            ByteBuffer buffer = parser.getDataFromRow(row);
            logger.info("buffer:{}",buffer);
            leafNode.putKeyAndValue(cursor,key,buffer);

            logger.info("pageBuffer:{}",leafNode.getPage());

            //存储到磁盘
            table.getPager().savePage(leafNode.getPage());

            //复位page数据
            leafNode.resetPage(table);
        }

    }

    private Cursor tableFind(Table table, Integer key){

        //TODO 暂定根节点
        TreeNode rootNode = table.getRootNode();
        LeafNode leafNode = ((LeafNode)rootNode);
        Integer cellNum = leafNode.getCellNum();
        logger.info("cell num:{}",cellNum);



        if(NodeTypeEnum.NODE_LEAF == rootNode.getNodeTypeEnum()){
            //叶子节点内部通过二分查找
            return binarySearch(table,key,leafNode,cellNum);
        } else {

            throw new IllegalStateException("暂未实现非叶子节点查找");
        }

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

    public Row selectById(Table table, Integer key){

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
        ByteBuffer pageBuffer = page.getPageBuffer().duplicate();

        pageBuffer.position(LeafNode.LEAF_NODE_HEADER_SIZE + cursor.getCellNo() * getKVSize(table));
        Row row = parser.getRowFromBuffer(table.getColumnList(),pageBuffer);

        return row;
    }

    public List<Row> select(Table table){

        //TODO 暂定根节点
        TreeNode rootNode = table.getRootNode();
        LeafNode leafNode = ((LeafNode)rootNode);
        Integer cellNum = leafNode.getCellNum();

        Page page = leafNode.getPage();
        ByteBuffer pageBuffer = page.getPageBuffer().duplicate();

        pageBuffer.position(LeafNode.LEAF_NODE_HEADER_SIZE);

        List<Row> rowList = Lists.newArrayList();

        for(int i = 0;i < cellNum; i++){
            logger.info(">>>>>cl:{}",table.getColumnList());
            logger.info(">>>>>pageBuffer:{}",pageBuffer);
            Row tmpRow = parser.getRowFromBuffer(table.getColumnList(),pageBuffer);

            rowList.add(tmpRow);
        }

        return rowList;
    }

    public JSONObject state(Table table){


        TreeNode rootNode = table.getRootNode();
        LeafNode leafNode = ((LeafNode)rootNode);
        Integer cellNum = leafNode.getCellNum();

        JSONObject json = new JSONObject();
        json.put("table",table.getTableName());
        json.put("cellNum",cellNum);

        return json;
    }

    /**
     * 叶子节点分裂
     * @param cursor
     * @param key
     * @param row
     */
    private void leafNodeSplitAndInsert(Cursor cursor,LeafNode leafNode,Integer key,Row row){

        boolean isRoot = leafNode.isRoot();
        if(isRoot){
            //如果原节点为根节点，则新建两个新leafNode 和一个新root节点
            Page rootPage = Page.EMPTY(0);
            TreeNode newRootNode = new InternalNode(rootPage,NodeTypeEnum.NODE_INTERNAL,true,-1);


        }

        int cellNum = leafNode.getCellNum();

        int leftNodeCellNum = (cellNum + 1)/2;
        int rightNodeCellNum = cellNum - leftNodeCellNum;

        //新增一个节点
        Integer pageNums = cursor.getTable().getPager().getPageNums().intValue();
        Page rightPage = Page.EMPTY(isRoot ? pageNums+2 : pageNums + 1); //如果是根节点，则需新建左右两个子节点
        LeafNode rightLeafNode = new LeafNode(rightPage,false,leafNode.getParent(),rightNodeCellNum);

        //copy数据到右节点
        Integer rightDataLen = rightNodeCellNum *(CELL_KEY_SIZE + cursor.getTable().getRowSize());
        byte[] rightData = leafNode.getData(rightDataLen,LeafNode.LEAF_NODE_HEADER_SIZE + leftNodeCellNum * (CELL_KEY_SIZE + cursor.getTable().getRowSize()));
        rightLeafNode.putData(rightData);

        //copy数据到左节点
        Page leftPage = Page.EMPTY(isRoot ? pageNums+1 : leafNode.getPage().getPageNo()); //如果是根节点，则需新建左右两个子节点
        LeafNode leftLeafNode = new LeafNode(leftPage,false,leafNode.getParent(),rightNodeCellNum);
        Integer leftDataLen = leftNodeCellNum *(CELL_KEY_SIZE + cursor.getTable().getRowSize());
        byte[] leftData = leafNode.getData(leftDataLen,LeafNode.LEAF_NODE_HEADER_SIZE);
        leftLeafNode.putData(leftData);



        //如果新插入的位置在左边
        if(cursor.getCellNo() < leftNodeCellNum){

            if()
            leftLeafNode.makeSpace(cursor);
        }else{
            //新插入的位置在右边
            int index = cursor.getCellNo() % leftNodeCellNum;

        }


    }

    private Integer getKVSize(Table table){

        return table.getRowSize() + CELL_KEY_SIZE;
    }
}
