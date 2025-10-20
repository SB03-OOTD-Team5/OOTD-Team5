package com.sprint.ootd5team.base.sse.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.base.sse.service.SseService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(controllers = SseController.class)
@DisplayName("SseController 슬라이스 테스트")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SseService sseService;

    @MockitoBean
    private AuthService authService;

    @Test
    void Sse_구독_요청_성공_lastEventId_존재() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();

        given(authService.getCurrentUserId()).willReturn(userId);
        given(sseService.connect(userId, lastEventId)).willReturn(new SseEmitter());

        // when & then
        mockMvc.perform(get("/api/sse")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("Last-Event-ID", lastEventId.toString()))
            .andExpect(status().isOk());
        then(sseService).should().connect(userId, lastEventId);
    }

    @Test
    void Sse_구독_요청_성공_lastEventId_미존재() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        given(authService.getCurrentUserId()).willReturn(userId);
        given(sseService.connect(userId, null)).willReturn(new SseEmitter());

        // when & then
        mockMvc.perform(get("/api/sse").accept(MediaType.TEXT_EVENT_STREAM))
            .andExpect(status().isOk());
        then(sseService).should().connect(userId, null);
    }

    @Test
    void Sse_에러_핸들링_동작_확인() throws Exception {
        mockMvc.perform(get("/api/sse/test-error")
                .accept(MediaType.TEXT_EVENT_STREAM))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(containsString("event: error")))
            .andExpect(content().string(containsString("data: 서버 오류가 발생했습니다.")));
    }
}
