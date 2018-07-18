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

package com.soyoung.battle.field;


import com.soyoung.battle.field.common.Nullable;
import com.soyoung.battle.field.common.logging.Loggers;
import com.soyoung.battle.field.rest.RestStatus;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public final class ExceptionsHelper {

    private static final Logger logger = Loggers.getLogger(ExceptionsHelper.class);

    public static RuntimeException convertToRuntime(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new BattlefieldException(e);
    }

    public static BattlefieldException convertToElastic(Exception e) {
        if (e instanceof BattlefieldException) {
            return (BattlefieldException) e;
        }
        return new BattlefieldException(e);
    }

    public static RestStatus status(Throwable t) {
        if (t != null) {
            if (t instanceof BattlefieldException) {
                return ((BattlefieldException) t).status();
            } else if (t instanceof IllegalArgumentException) {
                return RestStatus.BAD_REQUEST;
            }
        }
        return RestStatus.INTERNAL_SERVER_ERROR;
    }

    public static Throwable unwrapCause(Throwable t) {
        int counter = 0;
        Throwable result = t;
        while (result instanceof BattlefieldWrapperException) {
            if (result.getCause() == null) {
                return result;
            }
            if (result.getCause() == result) {
                return result;
            }
            if (counter++ > 10) {
                // dear god, if we got more than 10 levels down, WTF? just bail
                logger.warn("Exception cause unwrapping ran for 10 levels...", t);
                return result;
            }
            result = result.getCause();
        }
        return result;
    }


    /**
     * @deprecated Don't swallow exceptions, allow them to propagate.
     */
    @Deprecated
    public static String detailedMessage(Throwable t) {
        if (t == null) {
            return "Unknown";
        }
        if (t.getCause() != null) {
            StringBuilder sb = new StringBuilder();
            while (t != null) {
                sb.append(t.getClass().getSimpleName());
                if (t.getMessage() != null) {
                    sb.append("[");
                    sb.append(t.getMessage());
                    sb.append("]");
                }
                sb.append("; ");
                t = t.getCause();
                if (t != null) {
                    sb.append("nested: ");
                }
            }
            return sb.toString();
        } else {
            return t.getClass().getSimpleName() + "[" + t.getMessage() + "]";
        }
    }

    public static String stackTrace(Throwable e) {
        StringWriter stackTraceStringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stackTraceStringWriter);
        e.printStackTrace(printWriter);
        return stackTraceStringWriter.toString();
    }

    public static String formatStackTrace(final StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace).skip(1).map(e -> "\tat " + e).collect(Collectors.joining("\n"));
    }

    static final int MAX_ITERATIONS = 1024;

    /**
     * Unwrap the specified throwable looking for any suppressed errors or errors as a root cause of the specified throwable.
     *
     * @param cause the root throwable
     *
     * @return an optional error if one is found suppressed or a root cause in the tree rooted at the specified throwable
     */
    public static Optional<Error> maybeError(final Throwable cause, final Logger logger) {
        // early terminate if the cause is already an error
        if (cause instanceof Error) {
            return Optional.of((Error) cause);
        }

        final Queue<Throwable> queue = new LinkedList<>();
        queue.add(cause);
        int iterations = 0;
        while (!queue.isEmpty()) {
            iterations++;
            if (iterations > MAX_ITERATIONS) {
                logger.warn("giving up looking for fatal errors", cause);
                break;
            }
            final Throwable current = queue.remove();
            if (current instanceof Error) {
                return Optional.of((Error) current);
            }
            Collections.addAll(queue, current.getSuppressed());
            if (current.getCause() != null) {
                queue.add(current.getCause());
            }
        }
        return Optional.empty();
    }

    /**
     * Rethrows the first exception in the list and adds all remaining to the suppressed list.
     * If the given list is empty no exception is thrown
     *
     */
    public static <T extends Throwable> void rethrowAndSuppress(List<T> exceptions) throws T {
        T main = null;
        for (T ex : exceptions) {
            main = useOrSuppress(main, ex);
        }
        if (main != null) {
            throw main;
        }
    }

    /**
     * Throws a runtime exception with all given exceptions added as suppressed.
     * If the given list is empty no exception is thrown
     */
    public static <T extends Throwable> void maybeThrowRuntimeAndSuppress(List<T> exceptions) {
        T main = null;
        for (T ex : exceptions) {
            main = useOrSuppress(main, ex);
        }
        if (main != null) {
            throw new BattlefieldException(main);
        }
    }

    public static <T extends Throwable> T useOrSuppress(T first, T second) {
        if (first == null) {
            return second;
        } else {
            first.addSuppressed(second);
        }
        return first;
    }


    public static Throwable unwrap(Throwable t, Class<?>... clazzes) {
        if (t != null) {
            do {
                for (Class<?> clazz : clazzes) {
                    if (clazz.isInstance(t)) {
                        return t;
                    }
                }
            } while ((t = t.getCause()) != null);
        }
        return null;
    }

    /**
     * Throws the specified exception. If null if specified then <code>true</code> is returned.
     */
    public static boolean reThrowIfNotNull(@Nullable Throwable e) {
        if (e != null) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

}
