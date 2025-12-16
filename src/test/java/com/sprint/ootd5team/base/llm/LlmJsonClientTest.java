package com.sprint.ootd5team.base.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.clothes.LlmFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@DisplayName("LlmJsonClient 단위 테스트")
@ActiveProfiles("test")
public class LlmJsonClientTest {

    @Mock
    LlmProvider llmProvider;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LlmJsonClient llmJsonClient;

    private Prompt testPrompt;

    @BeforeEach
    void setUp() {
        testPrompt = new Prompt("Test prompt");
    }

    @Test
    @DisplayName("정상 JSON 응답을 파싱하여 객체 반환")
    void callJsonPrompt_returnsObject_onValidJson() throws Exception {
        // given
        String jsonResponse = "{\"name\":\"test\",\"value\":123}";
        given(llmProvider.chatCompletion(testPrompt)).willReturn(jsonResponse);

        // when
        TestDto result = llmJsonClient.callJsonPrompt(testPrompt, TestDto.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("test");
        assertThat(result.value).isEqualTo(123);
        verify(llmProvider).chatCompletion(testPrompt);
    }

    @Test
    @DisplayName("코드 펜스로 감싸진 JSON을 파싱")
    void callJsonPrompt_parsesJson_withCodeFence() throws Exception {
        // given
        String jsonResponse = "```json\n{\"name\":\"test\",\"value\":456}\n```";
        given(llmProvider.chatCompletion(testPrompt)).willReturn(jsonResponse);

        // when
        TestDto result = llmJsonClient.callJsonPrompt(testPrompt, TestDto.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("test");
        assertThat(result.value).isEqualTo(456);
    }

    @Test
    @DisplayName("추가 텍스트가 포함된 응답에서 JSON 객체 추출")
    void callJsonPrompt_extractsJson_fromMixedContent() throws Exception {
        // given
        String jsonResponse = "Here is your result: {\"name\":\"extracted\",\"value\":789} and some more text";
        given(llmProvider.chatCompletion(testPrompt)).willReturn(jsonResponse);

        // when
        TestDto result = llmJsonClient.callJsonPrompt(testPrompt, TestDto.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("extracted");
        assertThat(result.value).isEqualTo(789);
    }

    @Test
    @DisplayName("배열 형태의 JSON 파싱")
    void callJsonPrompt_parsesJsonArray() throws Exception {
        // given
        String jsonResponse = "[{\"name\":\"item1\",\"value\":1},{\"name\":\"item2\",\"value\":2}]";
        given(llmProvider.chatCompletion(testPrompt)).willReturn(jsonResponse);

        // when
        TestDto[] result = llmJsonClient.callJsonPrompt(testPrompt, TestDto[].class);

        // then
        assertThat(result).hasSize(2);
        assertThat(result[0].name).isEqualTo("item1");
        assertThat(result[1].name).isEqualTo("item2");
    }

    @Test
    @DisplayName("중첩된 JSON 객체 파싱")
    void callJsonPrompt_parsesNestedJson() throws Exception {
        // given
        String jsonResponse = "{\"outer\":{\"inner\":\"value\"}}";
        given(llmProvider.chatCompletion(testPrompt)).willReturn(jsonResponse);

        // when
        NestedDto result = llmJsonClient.callJsonPrompt(testPrompt, NestedDto.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.outer).isNotNull();
        assertThat(result.outer.inner).isEqualTo("value");
    }

    @Test
    @DisplayName("JSON 내부의 이스케이프된 따옴표 파싱")
    void callJsonPrompt_handlesEscapedQuotes() throws Exception {
        // given
        String jsonResponse = "{\"name\":\"test \\\"quoted\\\" value\",\"value\":999}";
        given(llmProvider.chatCompletion(testPrompt)).willReturn(jsonResponse);

        // when
        TestDto result = llmJsonClient.callJsonPrompt(testPrompt, TestDto.class);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name).isEqualTo("test \"quoted\" value");
    }

    @Test
    @DisplayName("빈 응답인 경우 LlmFailedException 발생")
    void callJsonPrompt_throwsException_onEmptyResponse() {
        // given
        given(llmProvider.chatCompletion(testPrompt)).willReturn("");

        // when & then
        assertThatThrownBy(() -> llmJsonClient.callJsonPrompt(testPrompt, TestDto.class))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    @DisplayName("null 응답인 경우 LlmFailedException 발생")
    void callJsonPrompt_throwsException_onNullResponse() {
        // given
        given(llmProvider.chatCompletion(testPrompt)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> llmJsonClient.callJsonPrompt(testPrompt, TestDto.class))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    @DisplayName("JSON 객체를 찾을 수 없으면 LlmFailedException 발생")
    void callJsonPrompt_throwsException_whenNoJsonFound() {
        // given
        given(llmProvider.chatCompletion(testPrompt)).willReturn("This is just plain text without JSON");

        // when & then
        assertThatThrownBy(() -> llmJsonClient.callJsonPrompt(testPrompt, TestDto.class))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    @DisplayName("잘못된 JSON 형식이면 LlmFailedException 발생")
    void callJsonPrompt_throwsException_onInvalidJson() {
        // given
        given(llmProvider.chatCompletion(testPrompt)).willReturn("{invalid json}");

        // when & then
        assertThatThrownBy(() -> llmJsonClient.callJsonPrompt(testPrompt, TestDto.class))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    @DisplayName("LlmProvider에서 예외가 발생하면 그대로 전파")
    void callJsonPrompt_propagatesException_fromProvider() {
        // given
        given(llmProvider.chatCompletion(testPrompt)).willThrow(LlmFailedException.emptyResponse());

        // when & then
        assertThatThrownBy(() -> llmJsonClient.callJsonPrompt(testPrompt, TestDto.class))
            .isInstanceOf(LlmFailedException.class);
    }

    public static class TestDto {
        public String name;
        public int value;
    }

    public static class NestedDto {
        public InnerDto outer;
    }

    public static class InnerDto {
        public String inner;
    }
}