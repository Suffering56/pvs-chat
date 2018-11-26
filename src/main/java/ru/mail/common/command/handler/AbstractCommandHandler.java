package ru.mail.common.command.handler;

import ru.mail.common.CommandParseException;
import ru.mail.common.protocol.Command;
import ru.mail.server.input.ChannelData;

import java.nio.channels.SocketChannel;
import java.util.Map;

public abstract class AbstractCommandHandler implements CommandHandler {

    @Override
    public void onCommandReadByServer(ChannelData sourceChannelData, Command command, Map<SocketChannel, ChannelData> channelDataMap, Map<String, ChannelData> channelDataMapByName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Command onConsoleInputByClient(String line) throws CommandParseException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCommandReadByClient(Command command, byte[] bytes) {
        throw new UnsupportedOperationException();
    }
}
