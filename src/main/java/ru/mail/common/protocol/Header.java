package ru.mail.common.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Header {

    private int contentLength;

    @JsonCreator
    public static Header valueOf(@JsonProperty("contentLength") int contentLength) {
        return new Header(contentLength);
    }
}
