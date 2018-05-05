package com.soyoung.battle.field.cli;


import com.soyoung.battle.field.cli.Command;

/**
 * An exception representing a user fixable problem in {@link Command} usage.
 */
public class UserException extends Exception {

    /** The exist status the cli should use when catching this user error. */
    public final int exitCode;

    /** Constructs a UserException with an exit status and message to show the user. */
    public UserException(int exitCode, String msg) {
        super(msg);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new user exception with specified exit status, message, and underlying cause.
     *
     * @param exitCode the exit code
     * @param msg      the message
     * @param cause    the underlying cause
     */
    public UserException(final int exitCode, final String msg, final Throwable cause) {
        super(msg, cause);
        this.exitCode = exitCode;
    }

}
