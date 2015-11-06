package net.techcable.jdiff.cmd;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

public class JDiff {
    private static final Map<String, Command> commands = Collections.synchronizedMap(new HashMap<>());

    public static void registerCommand(String name, Command command) {
        commands.put(name.toLowerCase(), command);
    }

    public static Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    static {
        registerCommand("help", new HelpCommand());
        registerCommand("diff", new DiffCommand());
        registerCommand("patch", new PatchCommand());
    }

    public static boolean debug;

    public static void main(String[] args) {
        if (args.length == 0) {
            printMainHelp();
            exit(1);
        }
        int usedArguments = 0;
        for (String argument : args) {
            if (!argument.startsWith("-")) break;
            usedArguments++;
            if (argument.equals("--")) break;
            if ("--debug".equals(argument)) {
                debug = true;
            }
        }
        String commandName = args[usedArguments++];
        args = Arrays.copyOfRange(args, usedArguments, args.length);
        Command command = getCommand(commandName);
        if (command == null) {
            print("There is no command named", commandName);
            print("See 'jdiff help' for available commands");
            exit(1);
        }
        try {
            command.execute(args);
        } catch (CommandArgumentException e) {
            if (debug) {
                e.printStackTrace();
            } else {
                print(e.getMessage());
            }
            exit(1);
        } catch (IOException e) {
            if (debug) {
                e.printStackTrace();
            } else {
                print("Error reading from stream", e.getMessage());
            }
            exit(1);
        } catch (Exception e) {
            if (debug) {
                e.printStackTrace();
            } else {
                print("Error:", e.getMessage());
            }
            exit(1);
        }
    }

    public static void printMainHelp() {
        print("Usage: jdiff [options] <command> [<args>]");
        print("Java diff utility");
        print();
        print("Available commands:");
        print("  diff     Create a difference between two files or directories");
        print("  patch    Apply a difference between two files or directories");
        print();
        print("Options:");
        print("  --debug  Print exception stacktraces");
    }

    // Printing Utilities

    public static void print() {
        System.out.println();
    }


    public static void print(String s) {
        System.out.println(s);
    }

    public static void print(String s, Object arg1) {
        synchronized (System.out) {
            System.out.print(s);
            System.out.print(' ');
            System.out.println(arg1);
        }
    }

    public static void print(String s, Object arg1, Object arg2) {
        synchronized (System.out) {
            System.out.print(s);
            System.out.print(' ');
            System.out.print(arg1);
            System.out.print(' ');
            System.out.println(arg2);
        }
    }

    public static void print(String s, Object... args) {
        StringBuilder builder = new StringBuilder(s);
        for (Object arg : args) {
            builder.append(' ');
            builder.append(arg);
        }
        print(builder.toString());
    }

}
