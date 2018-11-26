package ru.mail.server.repository.impl;

import org.springframework.stereotype.Component;
import ru.mail.common.config.annotations.PrintArgs;
import ru.mail.common.config.annotations.PrintableArg;
import ru.mail.server.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UserRepositoryImpl implements UserRepository {

    private Map<String, AtomicInteger> messagesCountMap = new HashMap<>();

    @Override
    public int getUserMessages(String login) {
        AtomicInteger counter = messagesCountMap.get(login);
        if (counter != null) {
            return counter.get();
        }
        return 0;
    }

    @Override
    public void incrementUserMessages(String login) {
        AtomicInteger counter = messagesCountMap.putIfAbsent(login, new AtomicInteger(1));
        if (counter != null) {
            counter.incrementAndGet();
        }
    }

    /**
     * В данном методе демонстрируется одна из возможностей применения собственных аннотаций.
     * Аннотация @PrintArgs логирует все аргументы метода, помеченные аннотацией @PrintableArg, с префиком prefix
     * Чтобы проверить работу - нужно:
     * -> запустить сервер -> запустить клиент
     * -> в клиенте залогиниться (/login username username)-> и выйти из клиента (/quit)
     * -> в результате в консоли сервера будет распечатано "disconnected user: username"
     */
    @Override
    @PrintArgs(prefix = "disconnected user: ")
    public void disconnectUser(@PrintableArg String login) {
        messagesCountMap.remove(login);
    }
}
