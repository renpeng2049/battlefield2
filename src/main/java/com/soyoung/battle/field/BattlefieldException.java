package com.soyoung.battle.field;

import com.soyoung.battle.field.rest.RestStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BattlefieldException extends RuntimeException {

    private final Map<String, List<String>> headers = new HashMap<>();

    /**
     * Returns a set of all header keys on this exception
     */
    public Set<String> getHeaderKeys() {
        return headers.keySet();
    }


    /**
     * Returns the list of header values for the given key or {@code null} if no header for the
     * given key exists.
     */
    public List<String> getHeader(String key) {
        return headers.get(key);
    }


    public BattlefieldException(){

    }


    /**
     * Construct a <code>ElasticsearchException</code> with the specified cause exception.
     */
    public BattlefieldException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a <code>ElasticsearchException</code> with the specified detail message.
     *
     * The message can be parameterized using <code>{}</code> as placeholders for the given
     * arguments
     *
     * @param msg  the detail message
     * @param args the arguments for the message
     */
    public BattlefieldException(String msg, Object... args) {
        super("todo expection message");
    }


    /**
     * Returns the rest status code associated with this exception.
     */
    public RestStatus status() {
        Throwable cause = unwrapCause();
        if (cause == this) {
            return RestStatus.INTERNAL_SERVER_ERROR;
        } else {
            return ExceptionsHelper.status(cause);
        }
    }


    /**
     * Unwraps the actual cause from the exception for cases when the exception is a
     * {@link }.
     *
     * @see ExceptionsHelper#unwrapCause(Throwable)
     */
    public Throwable unwrapCause() {
        return ExceptionsHelper.unwrapCause(this);
    }
}
