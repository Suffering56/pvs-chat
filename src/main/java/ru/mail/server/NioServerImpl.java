package ru.mail.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.mail.common.Constants;
import ru.mail.common.Utils;
import ru.mail.common.command.handler.CommandHandler;
import ru.mail.common.protocol.Command;
import ru.mail.common.protocol.CommandType;
import ru.mail.common.protocol.Header;
import ru.mail.server.input.ChannelData;
import ru.mail.server.input.InputCommandData;
import ru.mail.server.repository.UserRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

@SuppressWarnings({"MagicConstant", "ConstantConditions"})
@Component
public class NioServerImpl implements NioServer {

    private final ServerSocketChannel serverChannel;
    private final Selector selector;

    //SocketChannel can be used as key, because it doesn't override equals and hashcode
    private final Map<SocketChannel, ChannelData> channelDataMap = new ConcurrentHashMap<>();
    private final Map<String, ChannelData> channelDataMapByName = new ConcurrentHashMap<>();

    //buffer used by single thread
    private final ByteBuffer buffer = allocate(Constants.SERVER_BUFFER_SIZE);

    @Value("${socket.host}")
    private String host;
    @Value("${socket.port}")
    private Integer port;
    private Map<CommandType, CommandHandler> commandHandlerMap;
    private UserRepository userRepository;

    public NioServerImpl() throws Exception {
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        selector = SelectorProvider.provider().openSelector();
    }

    @PostConstruct
    private void init() throws IOException {
        serverChannel.socket().bind(new InetSocketAddress(host, port));
        serverChannel.register(selector, OP_ACCEPT);
    }

    @Override
    public void start() throws Exception {
        new BusinessLogicThread().start();

        while (serverChannel.isOpen()) {
            selector.select();  //waiting for any events
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    write(key);
                }
            }
        }
    }

    private class BusinessLogicThread extends Thread {
        @Override
        public void run() {
            while (true) {
                /**
                 * Можно создать например fixedThreadPool и всю обработку channelData выполнять в потоке из этого пула.
                 * Это позволит ускорить обработку бизнес-логики
                 */
                for (ChannelData channelData : channelDataMap.values()) {
                    InputCommandData commandData = channelData.peekInputFirst();

                    if (commandData != null && commandData.isFilled()) {
                        channelData.pollInputFirst();

                        Command command = Utils.deserializeCommand(commandData.getData());
                        CommandType commandType = command.getCommandType();

                        if (channelData.isAuthorized() || commandType == CommandType.LOGIN) {
                            commandHandlerMap.get(commandType).onCommandReadByServer(channelData, command, channelDataMap, channelDataMapByName);
                        } else {
                            channelData.putOutputCommand(Command.pleaseLoginCommand());
                        }
                    }
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE);

        channelDataMap.putIfAbsent(socketChannel, new ChannelData(selectionKey));
        ChannelData channelData = channelDataMap.get(socketChannel);

        //send login request
        channelData.putOutputCommand(Command.pleaseLoginCommand());
    }

    private void read(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.clear();
        int limit = 0;
        try {
            limit = socketChannel.read(buffer);
        } catch (IOException e) {
            disconnectUser(socketChannel);
        }

        if (limit > 0) {
            byte[] bytes = Utils.extractBytes(buffer);
//            System.out.println("bytes.toString() = " + new String(bytes));
            handleExtractedBytes(socketChannel, bytes);
        } else if (limit == -1) {
            disconnectUser(socketChannel);
        }
    }

    private void handleExtractedBytes(SocketChannel socketChannel, byte[] bytes) {
        ChannelData channelData = channelDataMap.get(socketChannel);

        if (channelData.isHeaderExpected()) {
            Header header = Utils.deserializeHeader(bytes);
            channelData.startDataProcessing(header.getContentLength());
        } else {
            InputCommandData inputCommandData = channelData.getCurrentInputData();
            inputCommandData.fillData(bytes);
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        ChannelData channelData = channelDataMap.get(socketChannel);
        Command command = channelData.pollOutputCommand();

        if (command != null) {
            Utils.sendCommand(socketChannel, command);
            key.interestOps(OP_READ);
        }
    }

    private void disconnectUser(SocketChannel socketChannel) throws IOException {
        socketChannel.close();
        ChannelData remove = channelDataMap.remove(socketChannel);
        String login = remove.getLogin();
        if (login != null) {
            channelDataMapByName.remove(login);
            userRepository.disconnectUser(login);
        }
    }

    @SuppressWarnings("unused")
    public void shutdown() throws IOException {
        serverChannel.close();
    }

    @Autowired
    public void setCommandHandlerMap(Map<CommandType, CommandHandler> commandHandlerMap) {
        this.commandHandlerMap = commandHandlerMap;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}