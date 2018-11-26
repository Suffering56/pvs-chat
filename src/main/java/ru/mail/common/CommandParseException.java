package ru.mail.common;

public class CommandParseException extends Exception {

    private CommandParseException(String message) {
        super(message);
    }

    public static CommandParseException invalidFormat() {
        return new CommandParseException("invalid command format");
    }

    public static CommandParseException unknownCommand() {
        return new CommandParseException("unknown command");
    }
}
