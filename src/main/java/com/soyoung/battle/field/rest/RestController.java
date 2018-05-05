package com.soyoung.battle.field.rest;

import com.soyoung.battle.field.common.io.Streams;
import com.soyoung.battle.field.http.HttpServerTransport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.soyoung.battle.field.rest.RestStatus.FORBIDDEN;
import static com.soyoung.battle.field.rest.RestStatus.INTERNAL_SERVER_ERROR;

public class RestController  implements HttpServerTransport.Dispatcher{

    final Logger logger = LogManager.getLogger(RestController.class);

    @Override
    public void dispatchRequest(RestRequest request, RestChannel channel) {

        logger.info("请求到达 >>>>>>");
        if (request.rawPath().equals("/favicon.ico")) {
            handleFavicon(request, channel);
            return;
        }

        handleBlankReq(request,channel);
    }

    @Override
    public void dispatchBadRequest(RestRequest request, RestChannel channel, Throwable cause) {

        logger.info("坏请求到达 >>>>>>");
    }

    void handleBlankReq(RestRequest request, RestChannel channel){

        try{
            String helloWorld = "Hello world";
            BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "text/plain", helloWorld.getBytes());
            channel.sendResponse(restResponse);
        }catch (Exception e){
            String error = "internal error";
            channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR, BytesRestResponse.TEXT_CONTENT_TYPE, error.getBytes()));
        }
    }

    void handleFavicon(RestRequest request, RestChannel channel) {
        if (request.method() == RestRequest.Method.GET) {
            try {
                try (InputStream stream = getClass().getResourceAsStream("/config/favicon.ico")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Streams.copy(stream, out);
                    BytesRestResponse restResponse = new BytesRestResponse(RestStatus.OK, "image/x-icon", out.toByteArray());
                    channel.sendResponse(restResponse);
                }
            } catch (IOException e) {
                String error = "internal error";
                channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR, BytesRestResponse.TEXT_CONTENT_TYPE, error.getBytes()));
            }
        } else {
            String error = "internal error";
            channel.sendResponse(new BytesRestResponse(FORBIDDEN, BytesRestResponse.TEXT_CONTENT_TYPE,  error.getBytes()));
        }
    }
}
