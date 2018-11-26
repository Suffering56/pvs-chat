package ru.mail.server.input;

import lombok.Getter;

@Getter
public class InputCommandData {
    @Getter
    final byte[] data;
    final int expectedLength;
    int actualLength;

    public boolean isFilled() {
        return actualLength == expectedLength;
    }

    public InputCommandData(int expectedLength) {
        this.expectedLength = expectedLength;
        data = new byte[expectedLength];
        actualLength = 0;
    }

    public void fillData(byte[] bytes) {
        System.arraycopy(bytes, 0, data, actualLength, bytes.length);
        actualLength += bytes.length;
    }
}