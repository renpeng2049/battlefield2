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

package com.soyoung.battle.field.transport.netty4;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Netty4Utils {


    public static void setup() {

    }

    public static void closeChannels(final Collection<Channel> channels) throws IOException {
        IOException closingExceptions = null;
        final List<ChannelFuture> futures = new ArrayList<>();
        for (final Channel channel : channels) {
            try {
                if (channel != null && channel.isOpen()) {
                    futures.add(channel.close());
                }
            } catch (Exception e) {
                if (closingExceptions == null) {
                    closingExceptions = new IOException("failed to close channels");
                }
                closingExceptions.addSuppressed(e);
            }
        }
        for (final ChannelFuture future : futures) {
            future.awaitUninterruptibly();
        }

        if (closingExceptions != null) {
            throw closingExceptions;
        }
    }

    /**
     * If the specified cause is an unrecoverable error, this method will rethrow the cause on a separate thread so that it can not be
     * caught and bubbles up to the uncaught exception handler.
     *
     * @param cause the throwable to test
     */
    public static void maybeDie(final Throwable cause) {

    }

}
