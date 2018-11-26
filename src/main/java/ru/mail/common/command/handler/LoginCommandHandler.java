package ru.mail.common.command.handler;

import org.springframework.stereotype.Service;
import ru.mail.common.CommandParam;
import ru.mail.common.CommandParseException;
import ru.mail.common.protocol.Command;
import ru.mail.common.protocol.CommandType;
import ru.mail.server.input.ChannelData;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("MagicConstant")
@Service
public class LoginCommandHandler extends AbstractCommandHandler {

    @Override
    public void onCommandReadByServer(ChannelData sourceChannelData, Command command, Map<SocketChannel, ChannelData> channelDataMap, Map<String, ChannelData> channelDataMapByName) {
        if (sourceChannelData.isAuthorized()) {
            sourceChannelData.putOutputCommand(Command.loginCommand("You are already authorized!"));
            return;
        }

        //TODO: can used in annotation @ValidLogin
        String login = command.getParam(CommandParam.LOGIN);
        String password = command.getParam(CommandParam.PASSWORD);

        if (Objects.equals(login, password)) {
            sourceChannelData.setLogin(login);
            channelDataMapByName.put(login, sourceChannelData);
            sourceChannelData.putOutputCommand(Command.loginCommand("Login accepted!"));
        } else {
            sourceChannelData.putOutputCommand(Command.loginCommand("Please, enter valid login or password:"));
        }
    }

    @Override
    public Command onConsoleInputByClient(String line) throws CommandParseException {
        String[] split = line.split(" ");
        if (split.length != 3) {
            throw CommandParseException.invalidFormat();
        }
        return Command
                .builder(CommandType.LOGIN, Command.SourceType.BY_CLIENT)
                .put(CommandParam.LOGIN, split[1])
                .put(CommandParam.PASSWORD, split[2])
                .build();
    }

    @Override
    public void onCommandReadByClient(Command command, byte[] bytes) {
        System.out.println("fromServer: " + command.getMessage());
    }
}
