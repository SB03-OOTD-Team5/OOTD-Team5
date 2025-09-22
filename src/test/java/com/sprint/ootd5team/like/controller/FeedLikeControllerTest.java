package com.sprint.ootd5team.like.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.config.SecurityConfig;
import com.sprint.ootd5team.base.security.JwtAuthenticationFilter;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.like.controller.FeedLikeController;
import com.sprint.ootd5team.domain.like.service.FeedLikeService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FeedLikeController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
    })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FeedLikeController 슬라이스 테스트")
@ActiveProfiles("test")
public class FeedLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedLikeService feedLikeService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("피드 좋아요 성공")
    void likeFeed_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(authService.getCurrentUserId()).willReturn(userId);
        willDoNothing().given(feedLikeService).like(feedId, userId);

        // when & then
        mockMvc.perform(post("/api/feeds/{feedId}/like", feedId))
            .andExpect(status().isNoContent());

        then(feedLikeService).should().like(feedId, userId);
    }

    @Test
    @DisplayName("피드 좋아요 취소 성공")
    void unLikeFeed_success() throws Exception {
        // given
        UUID feedId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(authService.getCurrentUserId()).willReturn(userId);
        willDoNothing().given(feedLikeService).unLike(feedId, userId);

        // when & then
        mockMvc.perform(delete("/api/feeds/{feedId}/like", feedId))
            .andExpect(status().isNoContent());

        then(feedLikeService).should().unLike(feedId, userId);
    }
}