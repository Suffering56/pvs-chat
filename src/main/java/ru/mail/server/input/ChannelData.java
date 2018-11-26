package ru.mail.server.input;

import lombok.Getter;
import lombok.Setter;
import ru.mail.common.protocol.Command;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelData {

    @Getter
    @Setter
    private String login = null;
    @Getter
    private SelectionKey selectionKey;
    @Getter
    private SocketChannel channel;

    private Deque<InputCommandData> inputQueue = new ArrayDeque<>();
    private Deque<Command> outputQueue = new ArrayDeque<>();
    private AtomicBoolean processed = new AtomicBoolean(false);

    public ChannelData(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.channel = (SocketChannel) selectionKey.channel();
    }

    public boolean isAuthorized() {
        return login != null;
    }

    public boolean isHeaderExpected() {
        InputCommandData inputCommandData = this.inputQueue.peekLast();

        if (inputCommandData == null) {
            return true;
        }
        return inputCommandData.isFilled();
    }

    public void startDataProcessing(int commandBytesLength) {
        inputQueue.add(new InputCommandData(commandBytesLength));
    }

    public InputCommandData getCurrentInputData() {
        return inputQueue.peekLast();
    }

    public InputCommandData peekInputFirst() {
        return inputQueue.peekFirst();
    }

    public InputCommandData pollInputFirst() {
        return inputQueue.pollFirst();
    }

    public void putOutputCommand(Command command) {
        outputQueue.add(command);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        selectionKey.selector().wakeup();
    }

    public Command pollOutputCommand() {
        return outputQueue.pollFirst();
    }

    @Deprecated
    public void unlock() {
        processed.set(false);
    }

    @Deprecated
    public boolean tryLock() {
        return processed.compareAndSet(false, true);
    }
}
