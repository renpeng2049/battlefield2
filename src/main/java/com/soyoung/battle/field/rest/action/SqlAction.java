package com.soyoung.battle.field.rest.action;

import com.alibaba.fastjson.JSONObject;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.rest.*;
import com.soyoung.battle.field.store.*;

import java.io.IOException;
import java.util.Map;

public class SqlAction extends BaseRestHandler {


    private StoreSchemas storeSchemas;
    private TableParser parser;
    private TxManager txManager;

    public SqlAction(Settings settings, RestController controller,StoreSchemas storeSchemas){
        super(settings);
        controller.registerHandler(RestRequest.Method.GET,"/sql",this);
        this.storeSchemas = storeSchemas;
        this.parser = new TableParser();
        this.txManager = new TxManager(parser);
    }

    @Override
    public String getName() {
        return "sql_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request) throws IOException {
        return channel -> {

            Table table = storeSchemas.getTableStruct("sample");
            JSONObject json = getParam(request);
            logger.info(">>>>>json :{} ",json);

            Row row = parser.parse2Row(table,json);

            txManager.insert(table,row);


            String helloWorld = "sql executed,ret:";
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        };
    }

}
