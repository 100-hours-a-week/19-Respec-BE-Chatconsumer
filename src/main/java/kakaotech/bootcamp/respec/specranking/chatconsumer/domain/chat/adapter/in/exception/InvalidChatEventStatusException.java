package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.exception;

public class InvalidChatEventStatusException extends ChatEventException {

    public static final String MESSAGE_INVALID_STATUS = "Invalid chat status";

    public InvalidChatEventStatusException(String message) {
        super(message);
    }
}
