package com.soyoung.battle.field.rest.action;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.rest.*;
import com.soyoung.battle.field.store.*;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class SelectAction extends BaseRestHandler {


    private StoreSchemas storeSchemas;
    private TableParser parser;
    private TxManager txManager;

    public SelectAction(Settings settings, RestController controller, StoreSchemas storeSchemas){
        super(settings);
        controller.registerHandler(RestRequest.Method.GET,"/select",this);
        this.storeSchemas = storeSchemas;
        this.parser = new TableParser();
        this.txManager = new TxManager(parser);
    }

    @Override
    public String getName() {
        return "select_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request) throws IOException {
        return channel -> {

            List<JSONObject> jsonList = Lists.newArrayList();

            Table table = storeSchemas.getTableStruct("sample");
            JSONObject json = getParam(request);

            String key = json.getString("id");
            if(StringUtils.isEmpty(key)){
                throw new IllegalStateException("id must not null");
            }


            if("*".equals(key)){
                //查询所有记录

                List<Row> rowList = txManager.select(table);


                for(Row row : rowList){
                    JSONObject tmpJson = new JSONObject();
                    List<Column> columnList = row.getColumnList();
                    for(Column column : columnList){
                        logger.info(">>>查询出的column:{}",column);
                        tmpJson.put(column.getName(),column.getValue());
                    }

                    jsonList.add(tmpJson);
                }

            }else {

                JSONObject retJson = new JSONObject();
                Row row = txManager.selectById(table, Integer.parseInt(key));

                for(Column column : row.getColumnList()){
                    retJson.put(column.getName(),column.getValue());
                }
                logger.info("retJson:{}",retJson);
                jsonList.add(retJson);
            }


            String helloWorld = jsonList.toString() ;
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        };
    }
}
