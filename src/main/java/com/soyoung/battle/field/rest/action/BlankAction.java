package com.soyoung.battle.field.rest.action;

import com.soyoung.battle.field.common.setting.Settings;
import com.soyoung.battle.field.rest.*;

import java.io.IOException;

public class BlankAction extends BaseRestHandler {

    public BlankAction(Settings settings, RestController controller){
        super(settings);
        controller.registerHandler(RestRequest.Method.GET,"/",this);
    }

    @Override
    public String getName() {
        return "blank_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request) throws IOException {
        return channel -> {
            String helloWorld = "Hello world";
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        };
    }
}
