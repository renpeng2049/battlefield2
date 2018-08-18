package com.soyoung.battle.field.rest.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.rest.*;
import com.soyoung.battle.field.store.StoreSchemas;
import com.soyoung.battle.field.store.Table;
import com.soyoung.battle.field.store.TableParser;
import com.soyoung.battle.field.store.TxManager;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

public class StateAction extends BaseRestHandler {


    private StoreSchemas storeSchemas;
    private TableParser parser;
    private TxManager txManager;

    public StateAction(Settings settings, RestController controller, StoreSchemas storeSchemas){
        super(settings);
        controller.registerHandler(RestRequest.Method.GET,"/state",this);
        this.storeSchemas = storeSchemas;
        this.parser = new TableParser();
        this.txManager = new TxManager(parser);
    }

    @Override
    public String getName() {
        return "state_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request) throws IOException {
        return channel -> {

            Table table = storeSchemas.getTableStruct("sample");

            JSONObject json = txManager.state(table);


            String helloWorld = json.toJSONString() ;
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        };
    }
}
