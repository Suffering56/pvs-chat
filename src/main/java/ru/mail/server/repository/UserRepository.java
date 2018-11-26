package ru.mail.server.repository;

public interface UserRepository {

    int getUserMessages(String name);

    void incrementUserMessages(String login);

    void disconnectUser(String login);
}
