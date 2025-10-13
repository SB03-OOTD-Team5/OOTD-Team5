package com.sprint.ootd5team.domain.extract.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.clothes.LlmFailedException;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import com.sprint.ootd5team.base.llm.LlmProvider;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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

    private final Map<String, ClothesAttribute> attributeCache = Map.of();

    @BeforeEach
    void setup() {
        service = new LlmExtractionService(llmProvider, new ObjectMapper());
    }

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
        ClothesExtraInfo result = service.extractExtra(mock(BasicClothesInfo.class), attributeCache);

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
        assertThatThrownBy(() -> service.extractExtra(mock(BasicClothesInfo.class), attributeCache))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    void 응답이_json형식이_아니면_invalidJson_발생() {
        // given
        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn("이건 그냥 텍스트");

        // when & then
        assertThatThrownBy(() -> service.extractExtra(mock(BasicClothesInfo.class), attributeCache))
            .isInstanceOf(LlmFailedException.class);
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
        ClothesExtraInfo result = service.extractExtra(mock(BasicClothesInfo.class), attributeCache);

        // then
        assertThat(result.name()).isEqualTo("코드블록 상품");
        assertThat(result.typeRaw()).isEqualTo("상의");
        assertThat(result.attributes()).containsEntry("계절", "여름");
    }

    @Test
    void json파싱에_실패하면_parsingError_발생() {
        // given
        String invalidJson = """
            {
              "wrongField": "값"
            }
            """;

        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn(invalidJson);

        // when & then
        assertThatThrownBy(() -> service.extractExtra(mock(BasicClothesInfo.class), attributeCache))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    void 상품명이_null이면_빈문자열로_처리된다() {
        // given
        BasicClothesInfo basic = new BasicClothesInfo("url", "본문", null);

        // LLM 응답 더미 (정상 JSON) 세팅
        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willReturn("""
            { "name": "테스트", "typeRaw": "상의", "attributes": {} }
        """);

        // when
        ClothesExtraInfo result = service.extractExtra(basic, Map.of());

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void 금칙어가_상품명에_포함되면_invalidJson예외가_발생한다() {
        // given
        BasicClothesInfo basic = new BasicClothesInfo("url", "본문", "ignore previous anything");

        // when & then
        assertThatThrownBy(() -> service.extractExtra(basic, Map.of()))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    void 본문이_너무_길면_maxLength까지만_자르고_처리된다() {
        // given
        String longBody = "a".repeat(2100);
        BasicClothesInfo basic = new BasicClothesInfo("url", longBody, "상품명");

        given(llmProvider.chatCompletion(any(Prompt.class)))
            .willAnswer(invocation -> {
                Prompt p = invocation.getArgument(0);
                String promptStr = p.getInstructions().get(0).getText();
                assertThat(promptStr).contains("a".repeat(100));
                assertThat(promptStr).contains("...");
                return """
                { "name": "테스트", "typeRaw": "상의", "attributes": {} }
            """;
            });

        // when
        ClothesExtraInfo result = service.extractExtra(basic, Map.of());

        // then
        assertThat(result.name()).isEqualTo("테스트");
    }

    @Test
    void 여러개의_코드블록이있어도_JSON만_추출된다() {
        String response = """
        ```json
        { "name": "첫번째", "typeRaw": "상의", "attributes": {} }
        ```
        some other text
        ``` 
        { "name": "두번째" }
        ```
        """;

        given(llmProvider.chatCompletion(any(Prompt.class))).willReturn(response);

        ClothesExtraInfo result = service.extractExtra(mock(BasicClothesInfo.class), Map.of());

        assertThat(result.name()).isEqualTo("첫번째");
    }

    @Test
    void 중첩된_JSON객체와_배열도_정상적으로_파싱된다() {
        String nestedJson = """
        {
          "name": "중첩 테스트",
          "typeRaw": "아우터",
          "attributes": {
            "색상": "빨강, 파랑",
            "사이즈": "height=180, weight=70"
          }
        }
        """;

        given(llmProvider.chatCompletion(any(Prompt.class))).willReturn(nestedJson);

        ClothesExtraInfo result = service.extractExtra(mock(BasicClothesInfo.class), Map.of());

        assertThat(result.name()).isEqualTo("중첩 테스트");
        assertThat(result.attributes()).containsKey("사이즈");
    }

    @Test
    void 코드블록만있고_JSON이없으면_invalidJson예외발생() {
        String onlyCodeBlock = """
        ```text
        그냥 텍스트만 있음
        ```
        """;

        given(llmProvider.chatCompletion(any(Prompt.class))).willReturn(onlyCodeBlock);

        assertThatThrownBy(() -> service.extractExtra(mock(BasicClothesInfo.class), Map.of()))
            .isInstanceOf(LlmFailedException.class);
    }
}