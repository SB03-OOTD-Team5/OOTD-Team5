package com.sprint.ootd5team.base.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmJsonClient {

    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper;

    /**
     * Prompt를 보내고 지정된 타입으로 응답 JSON 파싱
     */
    public <T> T callJsonPrompt(Prompt prompt, Class<T> responseType) {
        try {
            String result = llmProvider.chatCompletion(prompt);
            if (result == null || result.isBlank())
                throw ClothesExtractionFailedException.emptyResponse();

            String unwrapped = stripCodeFence(result);
            String jsonPayload = extractJsonObject(unwrapped);
            if (jsonPayload == null)
                throw ClothesExtractionFailedException.invalidJson();

            return objectMapper.readValue(jsonPayload, responseType);
        } catch (Exception e) {
            log.error("[LlmJsonClient] LLM JSON 호출 실패", e);
            throw ClothesExtractionFailedException.parsingError(e);
        }
    }

    /** 코드 블록 제거 */
    private String stripCodeFence(String response) {
        if (!response.startsWith("```")) return response;
        int closingFence = response.indexOf("```", 3);
        if (closingFence == -1) return response;
        String body = response.substring(3, closingFence);
        int newline = body.indexOf('\n');
        if (newline >= 0) body = body.substring(newline + 1);
        return body.trim();
    }

    /** JSON 객체 추출 */
    private String extractJsonObject(String response) {
        int depth = 0, start = -1;
        for (int i = 0; i < response.length(); i++) {
            char ch = response.charAt(i);
            if (ch == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (ch == '}') {
                if (--depth == 0 && start >= 0)
                    return response.substring(start, i + 1).trim();
            }
        }
        return null;
    }
}
