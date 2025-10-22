package com.sprint.ootd5team.base.llm;

import com.sprint.ootd5team.base.exception.clothes.LlmFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * {@link LlmProvider} 의 Gemini 구현체.
 * <p>
 * - Spring AI 의 {@link VertexAiGeminiChatModel} 을 래핑하여 LLM 호출 수행
 * - Prompt → ChatResponse → Text 변환
 * </p>
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.ai.provider", havingValue = "gemini")
@Profile("!test")
public class GeminiProvider implements LlmProvider {

    private final VertexAiGeminiChatModel geminiChatModel;

    public GeminiProvider(VertexAiGeminiChatModel geminiChatModel) {
        this.geminiChatModel = geminiChatModel;
    }

    @Override
    public String chatCompletion(Prompt prompt) {
        String promptPreview = prompt.getContents().toString();
        int promptLength = promptPreview.length();
        int promptHash = promptPreview.hashCode();
        log.debug("[GeminiProvider] 프롬프트 요청: length={}, hash={}", promptLength, promptHash);

        try {
            ChatResponse response = geminiChatModel.call(prompt);
            if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                String text = response.getResult().getOutput().getText();
                log.debug("[GeminiProvider] 응답 수신 완료: {}", text);
                return text != null ? text : "";
            } else {
                log.warn("[GeminiProvider] 빈 응답 수신: length={}, hash={}",
                    prompt.getContents().toString().length(),
                    prompt.getContents().toString().hashCode());
                throw LlmFailedException.emptyResponse();
            }
        } catch (Exception e) {
            log.error("[GeminiProvider] LLM 호출 실패", e);
            throw LlmFailedException.geminiCallFailed(e);
        }
    }
}