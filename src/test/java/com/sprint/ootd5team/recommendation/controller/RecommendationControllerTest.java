package com.sprint.ootd5team.recommendation.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.recommendation.controller.RecommendationController;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationDto;
import com.sprint.ootd5team.domain.recommendation.service.RecommendationService;
import java.util.List;
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
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RecommendationController 슬라이스 테스트")
@ActiveProfiles("test")
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("추천 API 호출 시 200 OK 를 반환한다")
    void 추천_API_호출시_200_OK를_반환한다() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID weatherId = UUID.randomUUID();
        RecommendationDto response = RecommendationDto.builder()
            .weatherId(weatherId)
            .userId(userId)
            .clothes(List.of())
            .build();

        given(authService.getCurrentUserId()).willReturn(userId);
        given(recommendationService.getRecommendation(weatherId, userId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/recommendations")
            .queryParam("weatherId", weatherId.toString())
            .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
        verify(authService).getCurrentUserId();
        verify(recommendationService).getRecommendation(weatherId, userId);
    }

    @Test
    @DisplayName("추천 API 는 로그인 사용자의 정보를 활용한다")
    void 추천_API는_로그인_사용자의_정보를_활용한다() throws Exception {
        // given
        UUID weatherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RecommendationDto response = RecommendationDto.builder()
            .weatherId(weatherId)
            .userId(userId)
            .clothes(List.of())
            .build();

        given(authService.getCurrentUserId()).willReturn(userId);
        given(recommendationService.getRecommendation(weatherId, userId)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/recommendations")
            .queryParam("weatherId", weatherId.toString())
            .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.weatherId").value(weatherId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()));
        verify(authService).getCurrentUserId();
        verify(recommendationService).getRecommendation(weatherId, userId);
    }

    @Test
    @DisplayName("필수 파라미터가 없으면 추천 API 가 실패한다")
    void 필수_파라미터가_없으면_추천_API가_실패한다() throws Exception {
        // given
        // when
        ResultActions result = mockMvc.perform(get("/api/recommendations")
            .accept(MediaType.APPLICATION_JSON));
        // then
        result.andExpect(status().isBadRequest());
    }
}
