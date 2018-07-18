package com.soyoung.battle.field.rest;

import com.soyoung.battle.field.common.Booleans;
import com.soyoung.battle.field.common.Nullable;
import io.netty.buffer.ByteBuf;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public abstract class RestRequest {


    private final Map<String, String> params;
    private final Map<String, List<String>> headers;
    private final String rawPath;
    private final Set<String> consumedParams = new HashSet<>();

    public RestRequest(String uri, Map<String, List<String>> headers){

        final Map<String, String> params = new HashMap<>();
        int pathEndPos = uri.indexOf('?');
        if (pathEndPos < 0) {
            this.rawPath = uri;
        } else {
            this.rawPath = uri.substring(0, pathEndPos);
            RestUtils.decodeQueryString(uri, pathEndPos + 1, params);
        }
        this.params = params;
        this.headers = Collections.unmodifiableMap(headers);
    }


    public enum Method {
        GET, POST, PUT, DELETE, OPTIONS, HEAD
    }

    public abstract Method method();

    /**
     * The uri of the rest request, with the query string.
     */
    public abstract String uri();


    /**
     * The non decoded, raw path provided.
     */
    public String rawPath() {
        return rawPath;
    }


    /**
     * The path part of the URI (without the query string), decoded.
     */
    public final String path() {
        return RestUtils.decodeComponent(rawPath());
    }

    public Map<String, String> params() {
        return params;
    }

    public abstract boolean hasContent();

    public abstract byte[] content();


    @Nullable
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Nullable
    public SocketAddress getLocalAddress() {
        return null;
    }

    /**
     * Get all values for the header or {@code null} if the header was not found
     */
    public final List<String> getAllHeaderValues(String name) {
        List<String> values = headers.get(name);
        if (values != null) {
            return Collections.unmodifiableList(values);
        }
        return null;
    }

    public boolean paramAsBoolean(String key, boolean defaultValue) {
        String rawParam = param(key);
        // Treat empty string as true because that allows the presence of the url parameter to mean "turn this on"
        if (rawParam != null && rawParam.length() == 0) {
            return true;
        } else {
            return Booleans.parseBoolean(rawParam, defaultValue);
        }
    }

    public final String param(String key) {
        consumedParams.add(key);
        return params.get(key);
    }


    /**
     * Returns a list of parameters that have been consumed. This method returns a copy, callers
     * are free to modify the returned list.
     *
     * @return the list of currently consumed parameters.
     */
    List<String> consumedParams() {
        return consumedParams.stream().collect(Collectors.toList());
    }

    /**
     * Returns a list of parameters that have not yet been consumed. This method returns a copy,
     * callers are free to modify the returned list.
     *
     * @return the list of currently unconsumed parameters.
     */
    List<String> unconsumedParams() {
        return params
                .keySet()
                .stream()
                .filter(p -> !consumedParams.contains(p))
                .collect(Collectors.toList());
    }


    /**
     * Get the value of the header or {@code null} if not found. This method only retrieves the first header value if multiple values are
     * sent. Use of {@link #getAllHeaderValues(String)} should be preferred
     */
    public final String header(String name) {
        List<String> values = headers.get(name);
        if (values != null && values.isEmpty() == false) {
            return values.get(0);
        }
        return null;
    }

}
