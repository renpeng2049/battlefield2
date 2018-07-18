package com.soyoung.battle.field.http.netty4;

import com.soyoung.battle.field.common.logging.Loggers;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.logging.log4j.Logger;

@ChannelHandler.Sharable
public class Netty4HttpRequestHandler extends SimpleChannelInboundHandler<Object> {

    private final Netty4HttpServerTransport serverTransport;
    private final boolean httpPipeliningEnabled;
    private final boolean detailedErrorsEnabled;


    Netty4HttpRequestHandler(Netty4HttpServerTransport serverTransport,boolean detailedErrorsEnabled){

        this.serverTransport = serverTransport;
        this.httpPipeliningEnabled = serverTransport.pipelining;
        this.detailedErrorsEnabled = detailedErrorsEnabled;
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Logger logger = Loggers.getLogger(Netty4HttpRequestHandler.class);

        final FullHttpRequest request;
        final HttpPipelinedRequest pipelinedRequest;
        if (this.httpPipeliningEnabled && msg instanceof HttpPipelinedRequest) {
            pipelinedRequest = (HttpPipelinedRequest) msg;
            request = (FullHttpRequest) pipelinedRequest.last();
        } else {
            pipelinedRequest = null;
            request = (FullHttpRequest) msg;
        }

        final FullHttpRequest copy =
                new DefaultFullHttpRequest(
                        request.protocolVersion(),
                        request.method(),
                        request.uri(),
                        Unpooled.copiedBuffer(request.content()),
                        request.headers(),
                        request.trailingHeaders());

        final Netty4HttpRequest httpRequest;

        try {
            httpRequest = new Netty4HttpRequest( copy, ctx.channel());
        } catch (Exception ex) {
            if (pipelinedRequest != null) {
                pipelinedRequest.release();
            }
            throw ex;
        }

        final Netty4HttpChannel channel =
                new Netty4HttpChannel(serverTransport, httpRequest, pipelinedRequest, detailedErrorsEnabled);

        if (request.decoderResult().isSuccess()) {

            serverTransport.dispatchRequest(httpRequest, channel);
        } else {
            assert request.decoderResult().isFailure();
            serverTransport.dispatchBadRequest(httpRequest, channel, request.decoderResult().cause());
        }
    }
}
