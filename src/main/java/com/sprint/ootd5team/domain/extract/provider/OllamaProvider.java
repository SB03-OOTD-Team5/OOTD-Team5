package com.sprint.ootd5team.domain.extract.provider;

import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * {@link LlmProvider} 의 Ollama 구현체.
 * <p>
 * - Spring AI 의 {@link OllamaChatModel} 을 래핑하여 LLM 호출을 수행
 * - Prompt → ChatResponse → Text 로 변환하여 반환
 * </p>
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.ai.provider", havingValue = "ollama")
public class OllamaProvider implements LlmProvider {
    private final OllamaChatModel ollamaChatModel;

    public OllamaProvider(OllamaChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    /**
     * Ollama LLM 서버에 프롬프트를 전달하고 응답 텍스트를 반환
     */
    @Override
    public String chatCompletion(Prompt prompt) {
        log.debug("[OllamaProvider] 프롬프트 요청: {}", prompt.getContents());
        try {
            ChatResponse response = ollamaChatModel.call(prompt);
            if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                String text = response.getResult().getOutput().getText();
                log.debug("[OllamaProvider] 응답 수신 완료: {}", text);
                return text != null ? text : "";
            } else {
                log.warn("[OllamaProvider] 빈 응답 수신 (prompt={})", prompt.getContents());
                return "";
            }
        } catch (Exception e) {
            log.error("[OllamaProvider] LLM 호출 실패", e);
            throw ClothesExtractionFailedException.ollamaCallFailed(e);
        }
    }
}
