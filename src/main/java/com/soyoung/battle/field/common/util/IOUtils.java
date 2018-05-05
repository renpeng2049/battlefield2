package com.soyoung.battle.field.common.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public class IOUtils {

    /**
     * Closes all given <tt>Closeable</tt>s.  Some of the
     * <tt>Closeable</tt>s may be null; they are
     * ignored.  After everything is closed, the method either
     * throws the first exception it hit while closing, or
     * completes normally if there were no exceptions.
     *
     * @param objects
     *          objects to call <tt>close()</tt> on
     */
    public static void close(Closeable... objects) throws IOException {
        close(Arrays.asList(objects));
    }

    /**
     * Closes all given <tt>Closeable</tt>s.
     * @see #close(Closeable...)
     */
    public static void close(Iterable<? extends Closeable> objects) throws IOException {
        Throwable th = null;

        for (Closeable object : objects) {
            try {
                if (object != null) {
                    object.close();
                }
            } catch (Throwable t) {
                addSuppressed(th, t);
                if (th == null) {
                    th = t;
                }
            }
        }

        if (th != null) {
            throw rethrowAlways(th);
        }
    }

    /** adds a Throwable to the list of suppressed Exceptions of the first Throwable
     * @param exception this exception should get the suppressed one added
     * @param suppressed the suppressed exception
     */
    private static void addSuppressed(Throwable exception, Throwable suppressed) {
        if (exception != null && suppressed != null) {
            exception.addSuppressed(suppressed);
        }
    }

    /**
     * This utility method takes a previously caught (non-null)
     * {@code Throwable} and rethrows either the original argument
     * if it was a subclass of the {@code IOException} or an
     * {@code RuntimeException} with the cause set to the argument.
     *
     * <p>This method <strong>never returns any value</strong>, even though it declares
     * a return value of type {@link Error}. The return value declaration
     * is very useful to let the compiler know that the code path following
     * the invocation of this method is unreachable. So in most cases the
     * invocation of this method will be guarded by an {@code if} and
     * used together with a {@code throw} statement, as in:
     * </p>
     * <pre>{@code
     *   if (t != null) throw IOUtils.rethrowAlways(t)
     * }
     * </pre>
     *
     * @param th The throwable to rethrow, <strong>must not be null</strong>.
     * @return This method always results in an exception, it never returns any value.
     *         See method documentation for detailsa and usage example.
     * @throws IOException if the argument was an instance of IOException
     * @throws RuntimeException with the {@link RuntimeException#getCause()} set
     *         to the argument, if it was not an instance of IOException.
     */
    public static Error rethrowAlways(Throwable th) throws IOException, RuntimeException {
        if (th == null) {
            throw new AssertionError("rethrow argument must not be null.");
        }

        if (th instanceof IOException) {
            throw (IOException) th;
        }

        if (th instanceof RuntimeException) {
            throw (RuntimeException) th;
        }

        if (th instanceof Error) {
            throw (Error) th;
        }

        throw new RuntimeException(th);
    }

    /**
     * Closes all given <tt>Closeable</tt>s, suppressing all thrown exceptions.
     * Some of the <tt>Closeable</tt>s may be null, they are ignored.
     *
     * @param objects
     *          objects to call <tt>close()</tt> on
     */
    public static void closeWhileHandlingException(Closeable... objects) {
        closeWhileHandlingException(Arrays.asList(objects));
    }

    /**
     * Closes all given <tt>Closeable</tt>s, suppressing all thrown exceptions.
     * @see #closeWhileHandlingException(Closeable...)
     */
    public static void closeWhileHandlingException(Iterable<? extends Closeable> objects) {
        for (Closeable object : objects) {
            try {
                if (object != null) {
                    object.close();
                }
            } catch (Throwable t) {
            }
        }
    }


}
