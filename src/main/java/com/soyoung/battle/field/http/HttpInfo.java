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

package com.soyoung.battle.field.http;

import com.soyoung.battle.field.transport.BoundTransportAddress;

public class HttpInfo  {

    private final BoundTransportAddress address;
    private final long maxContentLength;


    public HttpInfo(BoundTransportAddress address, long maxContentLength) {
        this.address = address;
        this.maxContentLength = maxContentLength;
    }

    static final class Fields {
        static final String HTTP = "http";
        static final String BOUND_ADDRESS = "bound_address";
        static final String PUBLISH_ADDRESS = "publish_address";
        static final String MAX_CONTENT_LENGTH = "max_content_length";
        static final String MAX_CONTENT_LENGTH_IN_BYTES = "max_content_length_in_bytes";
    }



    public BoundTransportAddress address() {
        return address;
    }

    public BoundTransportAddress getAddress() {
        return address();
    }
}
