package ru.mail.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.mail.common.CommandParseException;
import ru.mail.common.Utils;
import ru.mail.common.command.handler.CommandHandler;
import ru.mail.common.protocol.Command;
import ru.mail.common.protocol.CommandType;
import ru.mail.common.protocol.Header;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.ByteBuffer.allocate;
import static java.nio.channels.SelectionKey.*;

/**
 * Над реализацией клиентской части особо не заморачивался (времени не хватило)
 * Основной упор делал на сервере.
 */
@Component
public class NioClientImpl implements NioClient {

    private final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(2);
    //TODO: на самом деле здесь надо тоже как на сервере читать данные, но я просто не успел реализовать
    private final ByteBuffer buffer = allocate(8092);
    private final Selector selector;
    private final SocketChannel channel;

    private volatile AtomicBoolean waitForSendHeader = new AtomicBoolean(true);

    @Value("${socket.host}")
    private String host;
    @Value("${socket.port}")
    private Integer port;
    private Map<CommandType, CommandHandler> commandHandlerMap;

    public NioClientImpl() throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, OP_CONNECT);
    }

    @PostConstruct
    private void init() throws IOException {
        channel.connect(new InetSocketAddress(host, port));
    }

    @Override
    public void start() throws Exception {
        new ConsoleReaderThread().start();

        while (channel.isOpen()) {
            selector.select();

            for (SelectionKey selectionKey : selector.selectedKeys()) {
                try {
                    if (selectionKey.isConnectable()) {
                        finishConnect(channel);
                        selectionKey.interestOps(OP_WRITE);
                    } else if (selectionKey.isReadable()) {
                        read();
                    } else if (selectionKey.isWritable()) {
                        write(selectionKey);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void write(SelectionKey selectionKey) throws IOException {
        byte[] bytes = queue.poll();
        if (bytes != null) {
//            System.out.println("toServer: " + new String(bytes));
            Utils.sendBytes(channel, bytes);

            waitForSendHeader.compareAndSet(true, false);
        }
        selectionKey.interestOps(OP_READ);
    }

    private void read() throws IOException {
        buffer.clear();
        channel.read(buffer);
        byte[] bytes = Utils.extractBytes(buffer);
        if (bytes.length > 0) {
            try {
                Command command = Utils.deserializeCommand(bytes);
                commandHandlerMap.get(command.getCommandType()).onCommandReadByClient(command, bytes);
            } catch (Exception e) {
                System.err.println("error while handling command: " + new String(bytes));
                e.printStackTrace();
            }
        }
    }

    private class ConsoleReaderThread extends Thread {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);

            boolean isHeader = true;
            byte[] bytes = null;

            while (true) {
                try {
                    if (isHeader) {
                        String line = scanner.nextLine();
                        if (line == null || line.trim().isEmpty()) {
                            continue;
                        }
                        if (line.equals("/quit")) {
                            exit();
                        }

                        CommandType commandType = extractCommandType(line);
                        Command command = commandHandlerMap.get(commandType).onConsoleInputByClient(line);

                        bytes = Utils.serialize(command);

                        Header header = Header.valueOf(bytes.length);
                        queue.put(Utils.serialize(header));

                        isHeader = false;
                        waitForSendHeader.set(true);
                    } else {
                        while (waitForSendHeader.get()) {
                            Thread.sleep(3);
                        }
                        queue.put(bytes);
                        isHeader = true;
                    }

                    SelectionKey key = channel.keyFor(selector);
                    key.interestOps(OP_WRITE);
                    selector.wakeup();
                } catch (CommandParseException e) {
                    System.err.println(e.getMessage());
                } catch (NoSuchElementException e) {
                    //ignore
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private CommandType extractCommandType(String line) throws CommandParseException {
        String[] split = line.split(" ");
        if (split.length == 0) {
            throw CommandParseException.invalidFormat();
        }
        return CommandType.ofPrefix(split[0]);
    }

    private void finishConnect(SocketChannel channel) {
        try {
            channel.finishConnect();
        } catch (IOException e) {
            e.printStackTrace();
            exit();
        }
    }

    private void exit() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    @Autowired
    public void setCommandHandlerMap(Map<CommandType, CommandHandler> commandHandlerMap) {
        this.commandHandlerMap = commandHandlerMap;
    }
}
