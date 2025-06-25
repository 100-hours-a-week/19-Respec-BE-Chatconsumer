package kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.myserver.health.controller;

import static kakaotech.bootcamp.respec.specranking.chatconsumer.global.infrastructure.myserver.health.constant.HealthControllerTestConstant.PATH_URL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/health 요청 시 OK(200)을 반환한다")
    void health_check_request_returns_200() throws Exception {
        // when, then
        mockMvc.perform(get(PATH_URL))
                .andExpect(status().isOk());
    }

}
