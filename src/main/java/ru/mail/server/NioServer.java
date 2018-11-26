package ru.mail.server;

import org.springframework.stereotype.Component;

@Component
public interface NioServer {

    void start() throws Exception;
}
