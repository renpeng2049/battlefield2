package com.soyoung.battle.field.store;

import com.google.common.collect.Maps;
import com.soyoung.battle.field.btree.LeafNode;
import com.soyoung.battle.field.btree.NodeTypeEnum;
import com.soyoung.battle.field.btree.TreeNode;
import com.soyoung.battle.field.common.logging.Loggers;
import com.soyoung.battle.field.env.Environment;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Map;

public class StoreSchemas {

    private final Map<String,Table> tableMap = Maps.newConcurrentMap();


    public void registerTable(String tableName,Table table){
        tableMap.putIfAbsent(tableName, table);
    }

    public Table getTableStruct(String tableName){
        return tableMap.get(tableName);
    }

    public Map<String, Table> getStructMap() {
        return tableMap;
    }

    public void load(Environment environment){

        //构建sample
        Row row = new Row();
        row.addColumn(new Column<Integer>("id",Integer.class,null,0));
        row.addColumn(new Column<String>("name",String.class,32, row.getRowSize()));
        row.addColumn(new Column<String>("email",String.class,32, row.getRowSize()));

        Store store = new Store("sample",environment);
        store.load();

        //TODO 构建根节点,根节点的pageNo默认设置为0,当前为单节点树 20180805 16:55
        //TODO 正式构建根节点,可以通过parent node上溯找到

        Logger logger = Loggers.getLogger(StoreSchemas.class);

        Pager pager = new Pager(store);
        Page firstPage = pager.getPage(0);
        byte nodeType = -1;
        if(null == firstPage){
            //construct root node
            firstPage = Page.EMPTY(0);
            //根节点最开始也是叶子节点
            nodeType = 1;
        } else {
            nodeType = firstPage.getPageBuffer().get(0); //获取节点类型，position=0
        }

        //TODO 如果是非叶子节点，另行处理
        if(nodeType == NodeTypeEnum.NODE_INTERNAL.getIndex()){
            throw new IllegalStateException("暂未实现非叶子节点逻辑");
        }


        //获取node 数据条数
        Integer cellNum = firstPage.getPageBuffer().getInt(TreeNode.COMMON_HEADER_SIZE);

        //TODO 暂按叶子节点处理
        TreeNode rootNode = new LeafNode(firstPage,true,-1,cellNum);
        ((LeafNode) rootNode).setEndPostion(LeafNode.LEAF_NODE_HEADER_SIZE + cellNum * (row.getRowSize() + LeafNode.CELL_KEY_SIZE));
        logger.info("load 文件完成 ，rootPage:{}",rootNode.getPage());

        Table table = new Table("sample",row,rootNode,pager);

        registerTable("sample", table);

        //TODO 从文件中load 自定义schema信息
    }

}
