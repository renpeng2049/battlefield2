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

import com.alibaba.fastjson.JSONObject;
import com.soyoung.battle.field.common.CheckedConsumer;
import com.soyoung.battle.field.common.component.AbstractComponent;
import com.soyoung.battle.field.common.setting.Setting;
import com.soyoung.battle.field.common.setting.Settings;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Base handler for REST requests.
 * <p>
 * This handler makes sure that the headers &amp; context of the handled {@link RestRequest requests} are copied over to
 * the transport requests executed by the associated client. While the context is fully copied over, not all the headers
 * are copied, but a selected few. It is possible to control what headers are copied over by returning them in
 * {@link #()}.
 */
public abstract class BaseRestHandler extends AbstractComponent implements RestHandler {

    public static final Setting<Boolean> MULTI_ALLOW_EXPLICIT_INDEX =
        Setting.boolSetting("rest.action.multi.allow_explicit_index", true, Setting.Property.NodeScope);

    private final LongAdder usageCount = new LongAdder();

    protected BaseRestHandler(Settings settings) {
        super(settings);
    }

    public final long getUsageCount() {
        return usageCount.sum();
    }

    /**
     * @return the name of this handler. The name should be human readable and
     *         should describe the action that will performed when this API is
     *         called. This name is used in the response to the
     *         {@link }.
     */
    public abstract String getName();

    @Override
    public final void handleRequest(RestRequest request, RestChannel channel) throws Exception {
        // prepare the request for execution; has the side effect of touching the request parameters
        final RestChannelConsumer action = prepareRequest(request);

        // validate unconsumed params, but we must exclude params used to format the response
        // use a sorted set so the unconsumed parameters appear in a reliable sorted order
//        final SortedSet<String> unconsumedParams =
//            request.unconsumedParams().stream().filter(p -> !responseParams().contains(p)).collect(Collectors.toCollection(TreeSet::new));

        // validate the non-response params
//        if (!unconsumedParams.isEmpty()) {
//            final Set<String> candidateParams = new HashSet<>();
//            candidateParams.addAll(request.consumedParams());
//            candidateParams.addAll(responseParams());
//            throw new IllegalArgumentException(unrecognized(request, unconsumedParams, candidateParams, "parameter"));
//        }

        usageCount.increment();
        // execute the action
        action.accept(channel);
    }

    protected final String unrecognized(
        final RestRequest request,
        final Set<String> invalids,
        final Set<String> candidates,
        final String detail) {
        StringBuilder message = new StringBuilder(String.format(
            Locale.ROOT,
            "request [%s] contains unrecognized %s%s: ",
            request.path(),
            detail,
            invalids.size() > 1 ? "s" : ""));
        boolean first = true;

        return message.toString();
    }

    /**
     * REST requests are handled by preparing a channel consumer that represents the execution of
     * the request against a channel.
     */
    @FunctionalInterface
    protected interface RestChannelConsumer extends CheckedConsumer<RestChannel, Exception> {
    }

    /**
     * Prepare the request for execution. Implementations should consume all request params before
     * returning the runnable for actual execution. Unconsumed params will immediately terminate
     * execution of the request. However, some params are only used in processing the response;
     * implementations can override {@link BaseRestHandler#responseParams()} to indicate such
     * params.
     *
     * @param request the request to execute
     *  client  client for executing actions on the local node
     * @return the action to execute
     * @throws IOException if an I/O exception occurred parsing the request and preparing for
     *                     execution
     */
    protected abstract RestChannelConsumer prepareRequest(RestRequest request) throws IOException;

    /**
     * Parameters used for controlling the response and thus might not be consumed during
     * preparation of the request execution in
     * {@link BaseRestHandler#prepareRequest(RestRequest)}.
     *
     * @return a set of parameters used to control the response and thus should not trip strict
     * URL parameter checks.
     */
    protected Set<String> responseParams() {
        return Collections.emptySet();
    }

    protected JSONObject getParam(RestRequest request) {
        Map<String,String> param = request.params();
        byte[] content = request.content();

        JSONObject json = new JSONObject();

        if(null != content && content.length>0){
            json = JSONObject.parseObject(new String(content));
        }

        for(Map.Entry<String,String> entry : param.entrySet()){
            json.put(entry.getKey(),entry.getValue());
        }
        return json;
    }
}
