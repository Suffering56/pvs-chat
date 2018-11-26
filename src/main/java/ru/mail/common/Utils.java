package ru.mail.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import ru.mail.common.protocol.Command;
import ru.mail.common.protocol.Header;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Utils {

    //mapper is thread-safe
    private static final ObjectMapper mapper = new ObjectMapper();

    public static byte[] extractBytes(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        buffer.clear();
        return bytes;
    }

    public static void sendCommand(SocketChannel channel, Command command) throws IOException {
        sendBytes(channel, mapper.writeValueAsBytes(command));
    }

    public static void sendBytes(SocketChannel channel, byte[] bytes) throws IOException {
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        channel.write(wrap);
        wrap.rewind();
    }

    @SneakyThrows
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return mapper.readValue(bytes, clazz);
    }

    @SneakyThrows
    public static Command deserializeCommand(byte[] bytes) {
        return deserialize(bytes, Command.class);
    }

    @SneakyThrows
    public static Header deserializeHeader(byte[] bytes) {
        return deserialize(bytes, Header.class);
    }

    @SneakyThrows
    public static byte[] serialize(Object obj) {
        return mapper.writeValueAsBytes(obj);
    }
}
