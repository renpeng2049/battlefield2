package com.soyoung.battle.field.rest.action;

import com.alibaba.fastjson.JSONObject;
import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.rest.*;
import com.soyoung.battle.field.store.ArrayStore;
import com.soyoung.battle.field.store.StoreSchemas;
import com.soyoung.battle.field.store.TableStruct;
import com.soyoung.battle.field.store.TableStructParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class SqlAction extends BaseRestHandler {


    private ArrayStore arrayStore;
    private StoreSchemas storeSchemas;
    private TableStructParser parser;

    public SqlAction(Settings settings, RestController controller, ArrayStore arrayStore,StoreSchemas storeSchemas){
        super(settings);
        controller.registerHandler(RestRequest.Method.GET,"/sql",this);
        this.arrayStore = arrayStore;
        this.storeSchemas = storeSchemas;
        this.parser = new TableStructParser();
    }

    @Override
    public String getName() {
        return "sql_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request) throws IOException {
        return channel -> {

            TableStruct tableStruct = storeSchemas.getTableStruct("sample");
            Map<String,String> param = request.params();
            byte[] content = request.content();

            JSONObject json = new JSONObject();

            if(null != content && content.length>0){
                json = JSONObject.parseObject(new String(content));
            }

            for(Map.Entry<String,String> entry : param.entrySet()){
                json.put(entry.getKey(),entry.getValue());
            }
            logger.info(">>>>>json :{} ",json);

            ByteBuffer buffer = parser.parse2ByteBuff(json,tableStruct);
            logger.info(">>>>>buffer :{}",buffer);
            logger.info(">>>>>>buffer content:{}",new String(buffer.array()));


            boolean flag = arrayStore.append(buffer);

            String helloWorld = "sql executed,ret:" + flag;
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        };
    }
}
