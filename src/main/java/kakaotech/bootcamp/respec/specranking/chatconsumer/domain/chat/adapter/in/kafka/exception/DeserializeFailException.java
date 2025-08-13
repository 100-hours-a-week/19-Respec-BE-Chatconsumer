package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chat.adapter.in.kafka.exception;

public class DeserializeFailException extends ChatEventException {

    public static final String MESSAGE_DESERIALIZE_FAIL = "deserialize fail";

    public DeserializeFailException(String message) {
        super(message);
    }
}
