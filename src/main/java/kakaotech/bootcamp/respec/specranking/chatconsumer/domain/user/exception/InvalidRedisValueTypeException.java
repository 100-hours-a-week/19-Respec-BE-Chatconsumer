package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception;

public class InvalidRedisValueTypeException extends RuntimeException {
    public InvalidRedisValueTypeException(String message) {
        super(message);
    }
}
