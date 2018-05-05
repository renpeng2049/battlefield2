package com.soyoung.battle.field.rest;

import com.soyoung.battle.field.common.Nullable;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RestRequest {


    private final Map<String, String> params;
    private final Map<String, List<String>> headers;
    private final String rawPath;

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
}
