package ru.mail.common.protocol;

import ru.mail.common.CommandParseException;

public enum CommandType {

    /**
     * Обработчики (CommandHandlers) каждого типа команды объявлены в классе AbstractConfiguration
     */
    LOGIN,
    SEND_ALL,
    SEND_TO,
    INFO;

    public static CommandType ofPrefix(String prefix) throws CommandParseException {
        switch (prefix) {
            case "/login":
                return CommandType.LOGIN;
            case "/sendall":
                return CommandType.SEND_ALL;
            case "/sendto":
                return CommandType.SEND_TO;
            case "/info":
                return CommandType.INFO;
            default:
                throw CommandParseException.unknownCommand();
        }
    }
}
