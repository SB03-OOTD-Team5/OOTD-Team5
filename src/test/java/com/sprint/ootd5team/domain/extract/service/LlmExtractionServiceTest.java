package com.sprint.ootd5team.domain.extract.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import com.sprint.ootd5team.domain.extract.provider.LlmProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;

@ExtendWith(MockitoExtension.class)
class LlmExtractionServiceTest {

    @Mock
    private LlmProvider llmProvider;

    @InjectMocks
    private LlmExtractionService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 정상_json_응답시_clothesExtraInfo를_반환() {
        // given
        String validJson = """
            {
              "name": "테스트 자켓",
              "typeRaw": "아우터",
              "attributes": {
                "계절": "겨울",
                "색상": "블랙",
                "소재": "데님",
                "스타일": "캐주얼",
                "핏": "오버핏"
              }
            }
            """;
        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn(validJson);

        // when
        ClothesExtraInfo result = service.extractExtra(mock(BasicClothesInfo.class));

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("테스트 자켓");
        assertThat(result.typeRaw()).isEqualTo("아우터");
        assertThat(result.attributes()).containsEntry("색상", "블랙");
    }

    @Test
    void 빈_응답시_emptyResponse_발생() {
        // given
        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn("   ");

        // when & then
        assertThatThrownBy(() -> service.extractExtra(mock(BasicClothesInfo.class)))
            .isInstanceOf(ClothesExtractionFailedException.class);
    }

    @Test
    void 응답이_json형식이_아니면_emptyResponse_발생() {
        // given
        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn("이건 그냥 텍스트");

        // when & then
        assertThatThrownBy(() -> service.extractExtra(mock(BasicClothesInfo.class)))
            .isInstanceOf(ClothesExtractionFailedException.class);
    }

    @Test
    void 응답이_코드블록일경우_내부_Json만_파싱() {
        // given
        String wrappedJson = """
            ```json
            {
              "name": "코드블록 상품",
              "typeRaw": "상의",
              "attributes": { "계절": "여름" }
            }
            ```
            """;

        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn(wrappedJson);

        // when
        ClothesExtraInfo result = service.extractExtra(mock(BasicClothesInfo.class));

        // then
        assertThat(result.name()).isEqualTo("코드블록 상품");
        assertThat(result.typeRaw()).isEqualTo("상의");
        assertThat(result.attributes()).containsEntry("계절", "여름");
    }

    @Test
    void json파싱에_실패하면_eparsingError_발생() {
        // given
        String invalidJson = """
            {
              "wrongField": "값"
            }
            """;

        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn(invalidJson);

        // when & then
        assertThatThrownBy(() -> service.extractExtra(mock(BasicClothesInfo.class)))
            .isInstanceOf(ClothesExtractionFailedException.class);
    }
}
