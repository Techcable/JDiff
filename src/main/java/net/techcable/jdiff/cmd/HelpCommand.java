package net.techcable.jdiff.cmd;

public class HelpCommand implements Command {
    @Override
    public void execute(String[] arguments) {
        if (arguments.length == 0) {
            JDiff.printMainHelp();
        } else {
            String commandName = arguments[0];
            Command command = JDiff.getCommand(commandName);
            if (command == null) {
                error("Unknown command", commandName);
            }
            command.printHelp();
        }
    }

    @Override
    public void printHelp() {
        JDiff.print("Usage: jdiff help [<command>]");
        JDiff.print("Prints help for the specified command, or the whole app");
    }
}
