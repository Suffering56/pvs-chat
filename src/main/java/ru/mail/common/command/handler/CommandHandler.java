package ru.mail.common.command.handler;

import ru.mail.common.CommandParseException;
import ru.mail.common.protocol.Command;
import ru.mail.server.input.ChannelData;

import java.nio.channels.SocketChannel;
import java.util.Map;

public interface CommandHandler {

    void onCommandReadByServer(ChannelData sourceChannelData, Command command, Map<SocketChannel, ChannelData> channelDataMap, Map<String, ChannelData> channelDataMapByName);

    Command onConsoleInputByClient(String line) throws CommandParseException;

    void onCommandReadByClient(Command command, byte[] bytes);

}
