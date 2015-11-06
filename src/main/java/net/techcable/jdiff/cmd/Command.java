package net.techcable.jdiff.cmd;

import lombok.*;

import java.io.IOException;

public interface Command {
    public void execute(String[] arguments) throws IOException;

    public void printHelp();

    @SneakyThrows
    public default void error(Throwable t) {
        throw t;
    }

    public default void error(String s) {
        throw new CommandArgumentException(s);
    }

    public default void error(Object o, Object... args) {
        StringBuilder builder = new StringBuilder(String.valueOf(o));
        for (Object arg : args) {
            builder.append(' ');
            builder.append(arg);
        }
        error(builder.toString());
    }
}
