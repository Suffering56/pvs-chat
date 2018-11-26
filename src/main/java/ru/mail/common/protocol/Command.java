package ru.mail.common.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.mail.common.CommandParam.LOGIN;
import static ru.mail.common.CommandParam.MESSAGE;
import static ru.mail.common.CommandParam.SOURCE;

@Getter
@Setter
@NoArgsConstructor
public class Command {

    private CommandType commandType;
    private Map<String, Object> parameters = new HashMap<>();

    private Command(CommandType commandType) {
        this.commandType = commandType;
    }

    @JsonIgnore
    public String getMessage() {
        return getParam(MESSAGE);
    }

    @JsonIgnore
    public String getLogin() {
        return getParam(LOGIN);
    }

    @JsonIgnore
    public SourceType getSource() {
        return getParam(SOURCE);
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> T getParam(String paramName) {
        Objects.requireNonNull(parameters);
        return (T) parameters.get(paramName);
    }

    public static Command pleaseLoginCommand() {
        return loginCommand("Please, login:");
    }

    public static Command loginCommand(String message) {
        return builder(CommandType.LOGIN, SourceType.BY_SERVER)
                .message(message)
                .build();
    }

    public static Builder builder(CommandType commandType, SourceType sourceType) {
        return new Command(commandType)
                .new Builder()
                .put(SOURCE, sourceType);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class Builder {

        public Builder put(String paramName, Object value) {
            parameters.put(paramName, value);
            return this;
        }

        public Builder message(String message) {
            return put(MESSAGE, message);
        }

        public Builder login(String login) {
            return put(LOGIN, login);
        }

        public Command build() {
            return Command.this;
        }
    }

    public static enum SourceType {
        BY_SERVER, BY_CLIENT
    }
}
