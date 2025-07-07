package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.exception;

public class ChatEventException extends RuntimeException {
    public ChatEventException(String message) {
        super(message);
    }

    public ChatEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
