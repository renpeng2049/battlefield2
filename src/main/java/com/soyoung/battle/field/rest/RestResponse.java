/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.soyoung.battle.field.rest;

import com.soyoung.battle.field.BattlefieldException;
import com.soyoung.battle.field.common.Nullable;
import io.netty.buffer.ByteBuf;

import java.util.*;

public abstract class RestResponse {

    protected Map<String, List<String>> customHeaders;


    /**
     * The response content type.
     */
    public abstract String contentType();


    /**
     * The rest status code.
     */
    public abstract RestStatus status();

    public void copyHeaders(BattlefieldException ex) {
        Set<String> headerKeySet = ex.getHeaderKeys();
        if (customHeaders == null) {
            customHeaders = new HashMap<>(headerKeySet.size());
        }
        for (String key : headerKeySet) {
            List<String> values = customHeaders.get(key);
            if (values == null) {
                values = new ArrayList<>();
                customHeaders.put(key, values);
            }
            values.addAll(ex.getHeader(key));
        }
    }

    /**
     * Add a custom header.
     */
    public void addHeader(String name, String value) {
        if (customHeaders == null) {
            customHeaders = new HashMap<>(2);
        }
        List<String> header = customHeaders.get(name);
        if (header == null) {
            header = new ArrayList<>();
            customHeaders.put(name, header);
        }
        header.add(value);
    }

    /**
     * The response content. Note, if the content is {@link } it
     * should automatically be released when done by the channel sending it.
     */
    public abstract ByteBuf content();

    /**
     * Returns custom headers that have been added, or null if none have been set.
     */
    @Nullable
    public Map<String, List<String>> getHeaders() {
        return customHeaders;
    }
}
