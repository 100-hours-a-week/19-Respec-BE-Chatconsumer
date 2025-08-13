package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.exception;

public class UserNotFoundException extends UserException {

    public static final String MESSAGE_USER_NOT_FOUND = "User not found";

    public UserNotFoundException(String message, Long userId) {
        super(message + ", not found userId : " + userId);
    }
    
}
