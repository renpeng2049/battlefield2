package com.soyoung.battle.field.rest.action;

import com.alibaba.fastjson.JSONObject;
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

            Table table = storeSchemas.getTableStruct("sample");
            JSONObject json = getParam(request);

            String key = json.getString("id");
            if(StringUtils.isEmpty(key)){
                throw new IllegalStateException("id must not null");
            }

            txManager.select(table, Integer.parseInt(key));


            String helloWorld = "sql executed" ;
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        };
    }
}
