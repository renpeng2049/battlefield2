package com.soyoung.battle.field.rest.action;

import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.rest.*;
import com.soyoung.battle.field.store.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class SelectAction extends BaseRestHandler {


    private ArrayStore arrayStore;
    private StoreSchemas storeSchemas;
    private TableStructParser parser;

    public SelectAction(Settings settings, RestController controller, ArrayStore arrayStore,StoreSchemas storeSchemas){
        super(settings);
        controller.registerHandler(RestRequest.Method.GET,"/select",this);
        this.arrayStore = arrayStore;
        this.storeSchemas = storeSchemas;
        this.parser = new TableStructParser();
    }

    @Override
    public String getName() {
        return "select_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request) throws IOException {
        return channel -> {

            TableStruct tableStruct = storeSchemas.getTableStruct("sample");
            Map<String,String> param = request.params();

            ByteBuffer buffer = ByteBuffer.allocate(tableStruct.getRowSize());

            while(arrayStore.read(buffer)){

                buffer.flip();
                List<Column> columnList = parser.getColumnFromBuffer(tableStruct,buffer);

                for(Column column : columnList){

                    logger.info("column name:{},value:{}",column.getName(),column.getValue());
                }
                buffer.clear();
            }

            String helloWorld = "sql executed" ;
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        };
    }
}
