package com.soyoung.battle.field.rest;

import com.soyoung.battle.field.BattlefieldException;
import com.soyoung.battle.field.ExceptionsHelper;
import com.soyoung.battle.field.common.logging.Loggers;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BytesRestResponse extends RestResponse {

    public static final String TEXT_CONTENT_TYPE = "text/plain; charset=UTF-8";

    private static final String STATUS = "status";

    private final RestStatus status;
    private final ByteBuf content;
    private final String contentType;



    /**
     * Creates a new response based on {@link }.
     */
    public BytesRestResponse(RestStatus status, String content) {
        this(status,"text/plain", content.getBytes()); //TODO contentType 需抽象
    }

    /**
     * Creates a binary response.
     */
    public BytesRestResponse(RestStatus status, String contentType, byte[] content) {
        this.status = status;
        ByteBuf tmp = Unpooled.buffer();
        tmp.writeBytes(content);
        this.content = tmp;
        this.contentType = contentType;
    }


    public BytesRestResponse(RestChannel channel, Exception e) throws IOException {
        this(channel, ExceptionsHelper.status(e), e);
    }

    public BytesRestResponse(RestChannel channel, RestStatus status, Exception e) throws IOException {

        channel.request(); //TODO 获取请求内容
        this.status = status;

        ByteBuf tmp = Unpooled.buffer();
        tmp.writeBytes(e.toString().getBytes());
        this.content = tmp;
        this.contentType = "";

        if (e instanceof BattlefieldException) {
            copyHeaders(((BattlefieldException) e));
        }
    }


    @Override
    public String contentType() {
        return contentType;
    }

    @Override
    public RestStatus status() {
        return status;
    }

    @Override
    public ByteBuf content() {
        return content;
    }


    static BytesRestResponse createSimpleErrorResponse(RestChannel channel, RestStatus status, String errorMessage) throws IOException {
        return new BytesRestResponse(status, errorMessage);
    }
}
