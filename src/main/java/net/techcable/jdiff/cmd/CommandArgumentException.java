package net.techcable.jdiff.cmd;

import com.google.common.base.Joiner;

/**
 * Thrown to indicate an invalid command line
 */
public class CommandArgumentException extends RuntimeException {
    public CommandArgumentException(String s) {
        super(s);
    }
}
