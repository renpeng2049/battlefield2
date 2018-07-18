package com.soyoung.battle.field.http;

import com.soyoung.battle.field.common.util.concurrent.ThreadContext;
import com.soyoung.battle.field.rest.RestChannel;
import com.soyoung.battle.field.rest.RestRequest;
import com.soyoung.battle.field.transport.BoundTransportAddress;

public interface HttpServerTransport {


    String HTTP_SERVER_WORKER_THREAD_NAME_PREFIX = "http_server_worker";

    BoundTransportAddress boundAddress();

    HttpInfo info();

    HttpStats stats();

    /**
     * Dispatches HTTP requests.
     */
    interface Dispatcher {

        /**
         * Dispatches the {@link RestRequest} to the relevant request handler or responds to the given rest channel directly if
         * the request can't be handled by any request handler.
         *
         * @param request       the request to dispatch
         * @param channel       the response channel of this request
         */
        void dispatchRequest(RestRequest request, RestChannel channel);

        /**
         * Dispatches a bad request. For example, if a request is malformed it will be dispatched via this method with the cause of the bad
         * request.
         *
         * @param request       the request to dispatch
         * @param channel       the response channel of this request
         * @param cause         the cause of the bad request
         */
        void dispatchBadRequest(RestRequest request, RestChannel channel, Throwable cause);

    }

}
