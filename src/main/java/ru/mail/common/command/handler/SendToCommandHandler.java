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
public class SendToCommandHandler extends AbstractCommandHandler {

    private UserRepository userRepository;

    @Autowired
    public SendToCommandHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onCommandReadByServer(ChannelData sourceChannelData, Command command, Map<SocketChannel, ChannelData> channelDataMap, Map<String, ChannelData> channelDataMapByName) {
        String receiver = command.getParam(CommandParam.RECEIVER);
        ChannelData receiverData = channelDataMapByName.get(receiver);
        if (receiverData == null) {
            sourceChannelData.putOutputCommand(Command
                    .builder(CommandType.SEND_TO, Command.SourceType.BY_SERVER)
                    .message("user offline")
                    .build());
            return;
        }

        userRepository.incrementUserMessages(sourceChannelData.getLogin());

        receiverData.putOutputCommand(Command
                .builder(CommandType.SEND_TO, Command.SourceType.BY_SERVER)
                .message(command.getMessage())
                .login(sourceChannelData.getLogin())
                .build());
    }

    @Override
    public Command onConsoleInputByClient(String line) throws CommandParseException {
        String[] split = line.split(" ");
        if (split.length > 2) {
            if ("/sendto".equals(split[0])) {
                String receiver = split[1];
                String message = line.substring(9 + receiver.length());

                if (message.length() > 0) {
                    return Command
                            .builder(CommandType.SEND_TO, Command.SourceType.BY_CLIENT)
                            .message(message)
                            .put(CommandParam.RECEIVER, receiver)
                            .build();
                }
            }
        }
        throw CommandParseException.invalidFormat();
    }

    @Override
    public void onCommandReadByClient(Command command, byte[] bytes) {
        System.out.println("<" + command.getLogin() + ">: " + command.getMessage());
    }
}
