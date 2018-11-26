package ru.mail.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import ru.mail.common.config.AbstractConfiguration;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@SpringBootApplication
@ComponentScan()
public class ServerApp extends AbstractConfiguration implements CommandLineRunner {

    private NioServer nioServer;

    public static void main(String[] args) {
        SpringApplication.run(ServerApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        nioServer.start();
    }

    @Autowired
    public void setNioServer(NioServer nioServer) {
        this.nioServer = nioServer;
    }
}