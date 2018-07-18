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


import com.soyoung.battle.field.common.unit.ByteSizeValue;

import java.io.IOException;
import java.util.Locale;

/**
 * Class encapsulating stats about the circuit breaker
 */
public class CircuitBreakerStats {

    private final String name;
    private final long limit;
    private final long estimated;
    private final long trippedCount;
    private final double overhead;

    public CircuitBreakerStats(String name, long limit, long estimated, double overhead, long trippedCount) {
        this.name = name;
        this.limit = limit;
        this.estimated = estimated;
        this.trippedCount = trippedCount;
        this.overhead = overhead;
    }


    public String getName() {
        return this.name;
    }

    public long getLimit() {
        return this.limit;
    }

    public long getEstimated() {
        return this.estimated;
    }

    public long getTrippedCount() {
        return this.trippedCount;
    }

    public double getOverhead() {
        return this.overhead;
    }


    @Override
    public String toString() {
        return "[" + this.name +
                ",limit=" + this.limit + "/" + new ByteSizeValue(this.limit) +
                ",estimated=" + this.estimated + "/" + new ByteSizeValue(this.estimated) +
                ",overhead=" + this.overhead + ",tripped=" + this.trippedCount + "]";
    }

    static final class Fields {
        static final String LIMIT = "limit_size_in_bytes";
        static final String LIMIT_HUMAN = "limit_size";
        static final String ESTIMATED = "estimated_size_in_bytes";
        static final String ESTIMATED_HUMAN = "estimated_size";
        static final String OVERHEAD = "overhead";
        static final String TRIPPED_COUNT = "tripped";
    }
}
