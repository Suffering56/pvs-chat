package ru.mail.common.config;

import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import ru.mail.common.command.handler.*;
import ru.mail.common.config.annotations.PrintArgsAdvice;
import ru.mail.common.protocol.CommandType;
import ru.mail.server.repository.UserRepository;
import ru.mail.server.repository.impl.UserRepositoryImpl;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConfiguration {

    @Bean
    public ApplicationContextAware applicationContextAwareImpl() {
        return new ApplicationContextAwareImpl();
    }

    @Bean
    public Map<CommandType, CommandHandler> commandHandlerMap() {
        return new HashMap<CommandType, CommandHandler>() {{
            put(CommandType.LOGIN, new LoginCommandHandler());
            put(CommandType.SEND_ALL, new SendAllCommandHandler(userRepository()));
            put(CommandType.SEND_TO, new SendToCommandHandler(userRepository()));
            put(CommandType.INFO, new InfoCommandHandler(userRepository()));
        }};
    }

    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl();
    }

    @Bean
    public PrintArgsAdvice printArgsAdvice() {
        return new PrintArgsAdvice();
    }
}
