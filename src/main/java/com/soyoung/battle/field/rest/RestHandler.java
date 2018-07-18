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

/**
 * Handler for REST requests
 */
public interface RestHandler {

    /**
     * Handles a rest request.
     * @param request The request to handle
     * @param channel The channel to write the request response to
     *  client A client to use to make internal requests on behalf of the original request
     */
    void handleRequest(RestRequest request, RestChannel channel) throws Exception;

    default boolean canTripCircuitBreaker() {
        return true;
    }

    /**
     * Indicates if the RestHandler supports content as a stream. A stream would be multiple objects delineated by
     * {@link ()}. If a handler returns true this will affect the types of content that can be sent to
     * this endpoint.
     */
    default boolean supportsContentStream() {
        return false;
    }
}