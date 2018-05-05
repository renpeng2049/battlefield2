package com.soyoung.battle.field.rest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BytesRestResponse extends RestResponse {


    public static final String TEXT_CONTENT_TYPE = "text/plain; charset=UTF-8";

    private static final String STATUS = "status";

    private final RestStatus status;
    private final ByteBuf content;
    private final String contentType;

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
}
