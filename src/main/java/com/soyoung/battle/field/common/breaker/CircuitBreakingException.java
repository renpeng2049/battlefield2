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
package com.soyoung.battle.field.common.breaker;

import com.soyoung.battle.field.BattlefieldException;
import com.soyoung.battle.field.rest.RestStatus;


import java.io.IOException;

/**
 * Exception thrown when the circuit breaker trips
 */
public class CircuitBreakingException extends BattlefieldException {

    private final long bytesWanted;
    private final long byteLimit;

    public CircuitBreakingException(String message) {
        super(message);
        this.bytesWanted = 0;
        this.byteLimit = 0;
    }

    public CircuitBreakingException(String message, long bytesWanted, long byteLimit) {
        super(message);
        this.bytesWanted = bytesWanted;
        this.byteLimit = byteLimit;
    }


    public long getBytesWanted() {
        return this.bytesWanted;
    }

    public long getByteLimit() {
        return this.byteLimit;
    }

    @Override
    public RestStatus status() {
        return RestStatus.SERVICE_UNAVAILABLE;
    }
}
