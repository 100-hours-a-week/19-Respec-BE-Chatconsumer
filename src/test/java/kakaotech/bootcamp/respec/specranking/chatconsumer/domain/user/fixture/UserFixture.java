package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.fixture;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    private static final Long RECEIVER_ID = 1L;
    private static final String LOGIN_ID = "test@example.com";
    private static final String PASSWORD = "test-password";
    private static final String PROFILE_IMAGE_URL = "http://tes.com/example.jpg";
    private static final String NICKNAME = "test-nickname";
    private static final Boolean IS_OPEN_SPEC = true;
    private static final String PRIMARY_KEY_FIELD = "id";

    public static User create() {
        User testUser = new User(LOGIN_ID, PASSWORD, PROFILE_IMAGE_URL, NICKNAME, IS_OPEN_SPEC);
        ReflectionTestUtils.setField(testUser, PRIMARY_KEY_FIELD, RECEIVER_ID);
        return testUser;
    }
}
