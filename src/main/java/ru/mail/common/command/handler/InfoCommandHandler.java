package ru.mail.common.command.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mail.common.CommandParam;
import ru.mail.common.CommandParseException;
import ru.mail.common.protocol.Command;
import ru.mail.common.protocol.CommandType;
import ru.mail.server.input.ChannelData;
import ru.mail.server.repository.UserRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;

@Component
public class InfoCommandHandler extends AbstractCommandHandler {

    private UserRepository userRepository;

    @Autowired
    public InfoCommandHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onCommandReadByServer(ChannelData sourceChannelData, Command command, Map<SocketChannel, ChannelData> channelDataMap, Map<String, ChannelData> channelDataMapByName) {
        Command.Builder commandBuilder = Command.builder(CommandType.INFO, Command.SourceType.BY_SERVER);

        for (ChannelData channelData : channelDataMap.values()) {
            String login = channelData.getLogin();
            if (login != null) {
                StringBuilder userInfo = new StringBuilder();

                try {
                    int messagesCount = userRepository.getUserMessages(login);
                    InetSocketAddress remoteAddress = (InetSocketAddress) channelData.getChannel().getRemoteAddress();

                    userInfo.append("address = ").append(remoteAddress)
                            .append(" | messagesCount = ").append(messagesCount);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                commandBuilder.put("userInfo[" + login + "]", userInfo.toString());
            }
        }

        sourceChannelData.putOutputCommand(commandBuilder.build());
    }

    @Override
    public Command onConsoleInputByClient(String line) throws CommandParseException {
        if (!"/info".equals(line)) {
            throw CommandParseException.invalidFormat();
        }
        return Command
                .builder(CommandType.INFO, Command.SourceType.BY_CLIENT)
                .build();
    }

    @Override
    public void onCommandReadByClient(Command command, byte[] bytes) {
        command.getParameters().forEach((paramName, value) -> {
            if (!CommandParam.SOURCE.equals(paramName)) {
                System.out.println(paramName + ": " + value);
            }
        });
    }
}
