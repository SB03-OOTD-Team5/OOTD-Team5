package com.sprint.ootd5team.base.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.exception.clothes.LlmFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeminiProvider 단위 테스트")
@ActiveProfiles("test")
public class GeminiProviderTest {

    @Mock
    GoogleGenAiChatModel geminiChatModel;

    @InjectMocks
    GeminiProvider geminiProvider;

    private Prompt testPrompt;

    @BeforeEach
    void setUp() {
        testPrompt = new Prompt("Test prompt message");
    }

    @Test
    @DisplayName("정상 응답 시 텍스트 반환")
    void chatCompletion_returnsText_onSuccessResponse() {
        // given
        String expectedText = "This is a test response";

        ChatResponse chatResponse = org.mockito.Mockito.mock(ChatResponse.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        given(geminiChatModel.call(testPrompt)).willReturn(chatResponse);
        given(chatResponse.getResult().getOutput().getText()).willReturn(expectedText);

        // when
        String resultText = geminiProvider.chatCompletion(testPrompt);

        // then
        assertThat(resultText).isEqualTo(expectedText);
        verify(geminiChatModel).call(testPrompt);
    }

    @Test
    @DisplayName("응답 텍스트가 null이면 빈 문자열 반환")
    void chatCompletion_returnsEmptyString_whenTextIsNull() {
        ChatResponse chatResponse = org.mockito.Mockito.mock(ChatResponse.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        given(geminiChatModel.call(testPrompt)).willReturn(chatResponse);
        given(chatResponse.getResult().getOutput().getText()).willReturn(null);

        String resultText = geminiProvider.chatCompletion(testPrompt);

        assertThat(resultText).isEmpty();
    }

    @Test
    @DisplayName("ChatResponse가 null이면 LlmFailedException 발생")
    void chatCompletion_throwsException_whenResponseIsNull() {
        // given
        given(geminiChatModel.call(testPrompt)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> geminiProvider.chatCompletion(testPrompt))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    @DisplayName("Result(=Generation)가 null이면 LlmFailedException 발생")
    void chatCompletion_throwsException_whenResultIsNull() {
        // given
        ChatResponse chatResponse = org.mockito.Mockito.mock(ChatResponse.class);

        given(geminiChatModel.call(testPrompt)).willReturn(chatResponse);
        given(chatResponse.getResult()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> geminiProvider.chatCompletion(testPrompt))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    @DisplayName("Output이 null이면 LlmFailedException 발생")
    void chatCompletion_throwsException_whenOutputIsNull() {
        // given
        ChatResponse chatResponse = mock(ChatResponse.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        given(geminiChatModel.call(testPrompt)).willReturn(chatResponse);
        given(chatResponse.getResult().getOutput()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> geminiProvider.chatCompletion(testPrompt))
            .isInstanceOf(LlmFailedException.class);
    }

    @Test
    @DisplayName("Gemini API 호출 중 예외가 발생하면 LlmFailedException 발생")
    void chatCompletion_throwsException_onApiFailure() {
        // given
        given(geminiChatModel.call(testPrompt)).willThrow(new RuntimeException("API Error"));

        // when & then
        assertThatThrownBy(() -> geminiProvider.chatCompletion(testPrompt))
            .isInstanceOf(LlmFailedException.class);

        verify(geminiChatModel).call(testPrompt);
    }
}