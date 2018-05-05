package com.soyoung.battle.field;

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
}
