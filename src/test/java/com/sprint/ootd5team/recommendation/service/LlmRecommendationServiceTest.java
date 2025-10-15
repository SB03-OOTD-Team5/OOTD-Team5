package com.sprint.ootd5team.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.llm.LlmJsonClient;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.service.LlmRecommendationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
@DisplayName("LlmRecommendationService 단위 테스트")
class LlmRecommendationServiceTest {

    @Mock
    LlmJsonClient llmJsonClient;

    ObjectMapper objectMapper;

    LlmRecommendationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new LlmRecommendationService(llmJsonClient, objectMapper);
    }

    @Test
    void 정상응답시_ids_UUID로_파싱된다() {
        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        RecommendationInfoDto info = mock(RecommendationInfoDto.class);
        given(info.profileInfo()).willReturn(null);
        given(info.weatherInfo()).willReturn(null);
        given(llmJsonClient.callJsonPrompt(any(Prompt.class), any()))
            .willReturn(Map.of("ids", List.of(id1.toString(), id2.toString())));

        // when
        List<UUID> result = service.recommendOutfit(info, List.of());

        // then
        assertThat(result).containsExactly(id1, id2);
    }

    @Test
    void 잘못된UUID는_무시된다() {
        // given
        UUID valid = UUID.randomUUID();
        RecommendationInfoDto info = mock(RecommendationInfoDto.class);
        given(info.profileInfo()).willReturn(null);
        given(info.weatherInfo()).willReturn(null);
        given(llmJsonClient.callJsonPrompt(any(Prompt.class), any()))
            .willReturn(Map.of("ids", List.of(valid.toString(), "not-uuid", "another-bad")));

        // when
        List<UUID> result = service.recommendOutfit(info, List.of());

        // then
        assertThat(result).containsExactly(valid);
    }

    @Test
    void LLM예외시_빈리스트를_반환한다() {
        // given
        RecommendationInfoDto info = mock(RecommendationInfoDto.class);
        when(info.profileInfo()).thenReturn(null);
        when(info.weatherInfo()).thenReturn(null);
        willThrow(new RuntimeException("fail"))
            .given(llmJsonClient).callJsonPrompt(any(Prompt.class), any());

        // when
        List<UUID> result = service.recommendOutfit(info, List.of());

        // then
        assertThat(result).isEmpty();
    }
}