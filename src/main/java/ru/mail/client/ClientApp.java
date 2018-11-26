package ru.mail.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.mail.common.config.AbstractConfiguration;

@SpringBootApplication
public class ClientApp extends AbstractConfiguration implements CommandLineRunner {

    private NioClient nioClient;

    public static void main(String[] args) {
        SpringApplication.run(ClientApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        nioClient.start();
    }

    @Autowired
    public void setNioClient(NioClient nioClient) {
        this.nioClient = nioClient;
    }
}
