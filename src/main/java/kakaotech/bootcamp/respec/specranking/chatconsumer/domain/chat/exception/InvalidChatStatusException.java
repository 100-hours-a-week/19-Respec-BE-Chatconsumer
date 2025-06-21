package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.exception;

public class InvalidChatStatusException extends ChatException {

    public static final String MESSAGE_INVALID_STATUS = "Invalid chat status";

    public InvalidChatStatusException(String message) {
        super(message);
    }

    public InvalidChatStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
