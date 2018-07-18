package com.soyoung.battle.field.rest;

import com.soyoung.battle.field.common.Strings;
import com.soyoung.battle.field.common.breaker.CircuitBreaker;
import com.soyoung.battle.field.common.breaker.CircuitBreakerService;
import com.soyoung.battle.field.common.io.Streams;
import com.soyoung.battle.field.common.path.PathTrie;
import com.soyoung.battle.field.common.util.concurrent.ThreadContext;
import com.soyoung.battle.field.http.HttpServerTransport;
import com.soyoung.battle.field.store.ArrayStore;
import com.soyoung.battle.field.usage.UsageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.soyoung.battle.field.rest.BytesRestResponse.TEXT_CONTENT_TYPE;
import static com.soyoung.battle.field.rest.RestStatus.*;

public class RestController  implements HttpServerTransport.Dispatcher{

    final Logger logger = LogManager.getLogger(RestController.class);


    private final PathTrie<MethodHandlers> handlers = new PathTrie<>(RestUtils.REST_DECODER);

    private final CircuitBreakerService circuitBreakerService;

    private UsageService usageService;

    private final UnaryOperator<RestHandler> handlerWrapper;

    public RestController(UnaryOperator<RestHandler> handlerWrapper,CircuitBreakerService circuitBreakerService, UsageService usageService){
        this.circuitBreakerService = circuitBreakerService;
        this.handlerWrapper = handlerWrapper;
        this.usageService = usageService;
    }

    @Override
    public void dispatchRequest(RestRequest request, RestChannel channel) {

        logger.info("请求到达 >>>>>> path:{}",request.rawPath());
        logger.info("请求到达 >>>>>> param:{}",request.params());

        if (request.rawPath().equals("/config/favicon.ico")) {
            logger.info("获取icon ");
            handleFavicon(request, channel);
            return;
        }

        try {
            tryAllHandlers(request, channel);
        } catch (Exception e) {
            try {
                channel.sendResponse(new BytesRestResponse(channel, e));
            } catch (Exception inner) {
                inner.addSuppressed(e);
                logger.error((Supplier<?>) () ->
                        new ParameterizedMessage("failed to send failure response for uri [{}]", request.uri()), inner);
            }
        }
    }

    void tryAllHandlers(final RestRequest request, final RestChannel channel) throws Exception {

        // Request execution flag
        boolean requestHandled = false;

        if (checkErrorTraceParameter(request, channel) == false) {
            channel.sendResponse(
                    BytesRestResponse.createSimpleErrorResponse(channel, BAD_REQUEST, "error traces in responses are disabled."));
            return;
        }

        // Loop through all possible handlers, attempting to dispatch the request
        Iterator<MethodHandlers> allHandlers = getAllHandlers(request);
        for (Iterator<MethodHandlers> it = allHandlers; it.hasNext(); ) {
            final Optional<RestHandler> mHandler = Optional.ofNullable(it.next()).flatMap(mh -> mh.getHandler(request.method()));
            requestHandled = dispatchRequest(request, channel, mHandler);
            if (requestHandled) {
                break;
            }
        }

        // If request has not been handled, fallback to a bad request error.
        if (requestHandled == false) {
            handleBadRequest(request, channel);
        }
    }


    Iterator<MethodHandlers> getAllHandlers(final RestRequest request) {
        // Between retrieving the correct path, we need to reset the parameters,
        // otherwise parameters are parsed out of the URI that aren't actually handled.
        final Map<String, String> originalParams = new HashMap<>(request.params());
        return handlers.retrieveAll(getPath(request), () -> {
            // PathTrie modifies the request, so reset the params between each iteration
            request.params().clear();
            request.params().putAll(originalParams);
            return request.params();
        });
    }


    /**
     * Registers a REST handler to be executed when one of the provided methods and path match the request.
     *
     * @param path Path to handle (e.g., "/{index}/{type}/_bulk")
     * @param handler The handler to actually execute
     * @param method GET, POST, etc.
     */
    public void registerHandler(RestRequest.Method method, String path, RestHandler handler) {
        if (handler instanceof BaseRestHandler) {
            usageService.addRestHandler((BaseRestHandler) handler);
        }
        handlers.insertOrUpdate(path, new MethodHandlers(path, handler, method), (mHandlers, newMHandler) -> {
            return mHandlers.addMethods(handler, method);
        });
    }

    /**
     * Dispatch the request, if possible, returning true if a response was sent or false otherwise.
     */
    boolean dispatchRequest(final RestRequest request, final RestChannel channel,
                            final Optional<RestHandler> mHandler) throws Exception {
        final int contentLength = request.hasContent() ? request.content().length : 0;

        RestChannel responseChannel = channel;
        // Indicator of whether a response was sent or not
        boolean requestHandled;

        if (contentLength > 0 && mHandler.map(h -> hasContentType(request, h) == false).orElse(false)) {
            sendContentTypeErrorMessage(request, channel);
            requestHandled = true;
        } else if (contentLength > 0 && mHandler.map(h -> h.supportsContentStream()).orElse(false) &&
                !request.header("Content-Type").equals("text/plain")) {
            channel.sendResponse(BytesRestResponse.createSimpleErrorResponse(channel,
                    NOT_ACCEPTABLE, "Content-Type [" + request.header("Content-Type") +
                            "] does not support stream parsing. Use JSON or SMILE instead"));
            requestHandled = true;
        } else if (mHandler.isPresent()) {

            try {
                if (canTripCircuitBreaker(mHandler)) {
                    inFlightRequestsBreaker(circuitBreakerService).addEstimateBytesAndMaybeBreak(contentLength, "<http_request>");
                } else {
                    inFlightRequestsBreaker(circuitBreakerService).addWithoutBreaking(contentLength);
                }
                // iff we could reserve bytes for the request we need to send the response also over this channel
                responseChannel = new ResourceHandlingHttpChannel(channel, circuitBreakerService, contentLength);

                final RestHandler wrappedHandler = mHandler.map(h -> handlerWrapper.apply(h)).get();
                wrappedHandler.handleRequest(request, responseChannel);
                requestHandled = true;
            } catch (Exception e) {
                responseChannel.sendResponse(new BytesRestResponse(responseChannel, e));
                // We "handled" the request by returning a response, even though it was an error
                requestHandled = true;
            }
        } else {
            // Get the map of matching handlers for a request, for the full set of HTTP methods.
            final Set<RestRequest.Method> validMethodSet = getValidHandlerMethodSet(request);
            if (validMethodSet.size() > 0
                    && validMethodSet.contains(request.method()) == false
                    && request.method() != RestRequest.Method.OPTIONS) {
                // If an alternative handler for an explicit path is registered to a
                // different HTTP method than the one supplied - return a 405 Method
                // Not Allowed error.
                handleUnsupportedHttpMethod(request, channel, validMethodSet);
                requestHandled = true;
            } else if (validMethodSet.contains(request.method()) == false
                    && (request.method() == RestRequest.Method.OPTIONS)) {
                handleOptionsRequest(request, channel, validMethodSet);
                requestHandled = true;
            } else {
                requestHandled = false;
            }
        }
        // Return true if the request was handled, false otherwise.
        return requestHandled;
    }


    /**
     * If a request contains content, this method will return {@code true} if the {@code Content-Type} header is present, matches an
     * {@link } or the handler supports a content stream and the content type header is for newline delimited JSON,
     */
    private static boolean hasContentType(final RestRequest restRequest, final RestHandler restHandler) {
        if (restHandler.supportsContentStream() && restRequest.header("Content-Type") != null) {
            return true;
        }
        return false;

    }

    /**
     * Get the valid set of HTTP methods for a REST request.
     */
    private Set<RestRequest.Method> getValidHandlerMethodSet(RestRequest request) {
        Set<RestRequest.Method> validMethods = new HashSet<>();
        Iterator<MethodHandlers> allHandlers = getAllHandlers(request);
        for (Iterator<MethodHandlers> it = allHandlers; it.hasNext(); ) {
            Optional.ofNullable(it.next()).map(mh -> validMethods.addAll(mh.getValidMethods()));
        }
        return validMethods;
    }


    /**
     * Handle requests to a valid REST endpoint using an unsupported HTTP
     * method. A 405 HTTP response code is returned, and the response 'Allow'
     * header includes a list of valid HTTP methods for the endpoint (see
     * <a href="https://tools.ietf.org/html/rfc2616#section-10.4.6">HTTP/1.1 -
     * 10.4.6 - 405 Method Not Allowed</a>).
     */
    private void handleUnsupportedHttpMethod(RestRequest request, RestChannel channel, Set<RestRequest.Method> validMethodSet) {
        try {
            BytesRestResponse bytesRestResponse = BytesRestResponse.createSimpleErrorResponse(channel, METHOD_NOT_ALLOWED,
                    "Incorrect HTTP method for uri [" + request.uri() + "] and method [" + request.method() + "], allowed: " + validMethodSet);
            bytesRestResponse.addHeader("Allow", Strings.collectionToDelimitedString(validMethodSet, ","));
            channel.sendResponse(bytesRestResponse);
        } catch (final IOException e) {
            logger.warn("failed to send bad request response", e);
            channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR, TEXT_CONTENT_TYPE, "null".getBytes()));
        }
    }


    /**
     * Handle HTTP OPTIONS requests to a valid REST endpoint. A 200 HTTP
     * response code is returned, and the response 'Allow' header includes a
     * list of valid HTTP methods for the endpoint (see
     * <a href="https://tools.ietf.org/html/rfc2616#section-9.2">HTTP/1.1 - 9.2
     * - Options</a>).
     */
    private void handleOptionsRequest(RestRequest request, RestChannel channel, Set<RestRequest.Method> validMethodSet) {
        if (request.method() == RestRequest.Method.OPTIONS && validMethodSet.size() > 0) {
            BytesRestResponse bytesRestResponse = new BytesRestResponse(OK, TEXT_CONTENT_TYPE, "null".getBytes());
            bytesRestResponse.addHeader("Allow", Strings.collectionToDelimitedString(validMethodSet, ","));
            channel.sendResponse(bytesRestResponse);
        } else if (request.method() == RestRequest.Method.OPTIONS && validMethodSet.size() == 0) {
            /*
             * When we have an OPTIONS HTTP request and no valid handlers,
             * simply send OK by default (with the Access Control Origin header
             * which gets automatically added).
             */
            channel.sendResponse(new BytesRestResponse(OK, TEXT_CONTENT_TYPE, "null".getBytes()));
        }
    }

    private String getPath(RestRequest request) {
        // we use rawPath since we don't want to decode it while processing the path resolution
        // so we can handle things like:
        // my_index/my_type/http%3A%2F%2Fwww.google.com
        return request.rawPath();
    }


    /**
     * Handle a requests with no candidate handlers (return a 400 Bad Request
     * error).
     */
    private void handleBadRequest(RestRequest request, RestChannel channel) {
        channel.sendResponse(new BytesRestResponse(BAD_REQUEST,
                "No handler found for uri [" + request.uri() + "] and method [" + request.method() + "]"));
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

//            Integer id = 1;
//            String name = "renpeng";
//            String email = "493252378@qq.com";
//
//            SampleSchema ss = new SampleSchema();
//
//            ByteBuffer bb = ByteBuffer.allocate(ss.getRowSize());
//            bb.putInt(id);
//            bb.put(name.getBytes());
//            byte[] emailByte = email.getBytes();
//            bb.put(emailByte,36,emailByte.length);



        }catch (Exception e){
            String error = "internal error";
            channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR, TEXT_CONTENT_TYPE, error.getBytes()));
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
                channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR, TEXT_CONTENT_TYPE, error.getBytes()));
            }
        } else {
            String error = "internal error";
            channel.sendResponse(new BytesRestResponse(FORBIDDEN, TEXT_CONTENT_TYPE,  error.getBytes()));
        }
    }


    /**
     * Checks the request parameters against enabled settings for error trace support
     * @return true if the request does not have any parameters that conflict with system settings
     */
    boolean checkErrorTraceParameter(final RestRequest request, final RestChannel channel) {
        // error_trace cannot be used when we disable detailed errors
        // we consume the error_trace parameter first to ensure that it is always consumed
        if (request.paramAsBoolean("error_trace", false) && channel.detailedErrorsEnabled() == false) {
            return false;
        }

        return true;
    }


    private void sendContentTypeErrorMessage(RestRequest restRequest, RestChannel channel) throws IOException {
        final List<String> contentTypeHeader = restRequest.getAllHeaderValues("Content-Type");
        final String errorMessage;
        if (contentTypeHeader == null) {
            errorMessage = "Content-Type header is missing";
        } else {
            errorMessage = "Content-Type header [" +
                    Strings.collectionToCommaDelimitedString(restRequest.getAllHeaderValues("Content-Type")) + "] is not supported";
        }

        channel.sendResponse(BytesRestResponse.createSimpleErrorResponse(channel, NOT_ACCEPTABLE, errorMessage));
    }


    /**
     * @return true iff the circuit breaker limit must be enforced for processing this request.
     */
    public boolean canTripCircuitBreaker(final Optional<RestHandler> handler) {
        return handler.map(h -> h.canTripCircuitBreaker()).orElse(true);
    }

    private static final class ResourceHandlingHttpChannel implements RestChannel {
        private final RestChannel delegate;
        private final CircuitBreakerService circuitBreakerService;
        private final int contentLength;
        private final AtomicBoolean closed = new AtomicBoolean();

        ResourceHandlingHttpChannel(RestChannel delegate, CircuitBreakerService circuitBreakerService, int contentLength) {
            this.delegate = delegate;
            this.circuitBreakerService = circuitBreakerService;
            this.contentLength = contentLength;
        }

        @Override
        public RestRequest request() {
            return delegate.request();
        }

        @Override
        public boolean detailedErrorsEnabled() {
            return delegate.detailedErrorsEnabled();
        }

        @Override
        public void sendResponse(RestResponse response) {
            close();
            delegate.sendResponse(response);
        }

        private void close() {
            // attempt to close once atomically
            if (closed.compareAndSet(false, true) == false) {
                throw new IllegalStateException("Channel is already closed");
            }
            inFlightRequestsBreaker(circuitBreakerService).addWithoutBreaking(-contentLength);
        }

    }


    private static CircuitBreaker inFlightRequestsBreaker(CircuitBreakerService circuitBreakerService) {
        // We always obtain a fresh breaker to reflect changes to the breaker configuration.
        return circuitBreakerService.getBreaker(CircuitBreaker.IN_FLIGHT_REQUESTS);
    }
}
