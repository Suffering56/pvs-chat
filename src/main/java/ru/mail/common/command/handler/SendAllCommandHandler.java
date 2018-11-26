package ru.mail.common.command.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mail.common.CommandParam;
import ru.mail.common.CommandParseException;
import ru.mail.common.protocol.Command;
import ru.mail.common.protocol.CommandType;
import ru.mail.server.input.ChannelData;
import ru.mail.server.repository.UserRepository;

import java.nio.channels.SocketChannel;
import java.util.Map;

@Service
public class SendAllCommandHandler extends AbstractCommandHandler {

    private UserRepository userRepository;

    @Autowired
    public SendAllCommandHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onCommandReadByServer(ChannelData sourceChannelData, Command command, Map<SocketChannel, ChannelData> channelDataMap, Map<String, ChannelData> channelDataMapByName) {
        userRepository.incrementUserMessages(sourceChannelData.getLogin());

        for (ChannelData anotherChannelData : channelDataMap.values()) {
            if (anotherChannelData != sourceChannelData) {
                anotherChannelData.putOutputCommand(Command
                        .builder(CommandType.SEND_ALL, Command.SourceType.BY_SERVER)
                        .message(command.getMessage())
                        .login(sourceChannelData.getLogin())
                        .build());
            }
        }
    }

    @Override
    public Command onConsoleInputByClient(String line) throws CommandParseException {
        String message = line.substring("/sendall ".length());
        if (message.length() > 0) {
            return Command
                    .builder(CommandType.SEND_ALL, Command.SourceType.BY_CLIENT)
                    .message(message)
                    .build();
        } else {
            throw CommandParseException.invalidFormat();
        }
    }

    @Override
    public void onCommandReadByClient(Command command, byte[] bytes) {
        System.out.println("<" + command.getLogin() + ">: " + command.getMessage());
    }
}
