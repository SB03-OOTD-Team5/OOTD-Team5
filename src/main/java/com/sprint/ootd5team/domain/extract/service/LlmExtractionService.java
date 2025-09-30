package com.sprint.ootd5team.domain.extract.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import com.sprint.ootd5team.domain.extract.provider.LlmProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * LLM을 이용해 의상 본문 텍스트에서 추가 정보를 추출하는 서비스
 * <p>
 * - 입력 텍스트를 기반으로 JSON 포맷 지시문을 포함한 Prompt 생성
 * - {@link LlmProvider} 를 통해 LLM 호출
 * - 응답을 {@link ClothesExtraInfo} 로 파싱하여 반환
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmExtractionService {

    private final LlmProvider llmProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClothesExtraInfo extractExtra(BasicClothesInfo basic) {
        // 1. 입력 데이터
        String llmInput = buildLlmInput(basic);

        // 2. Prompt 생성
        Prompt chatPrompt = buildPrompt(llmInput);
        log.debug("[LlmExtractionService] prompt=\n{}", chatPrompt);

        // 3. LLM 호출
        String result = llmProvider.chatCompletion(chatPrompt);
        log.debug("[LlmExtractionService] LLM 원본 응답: {}", result);

        if (result == null || result.isBlank()) {
            log.error("[LlmExtractionService] LLM 응답이 비어 있음");
            throw ClothesExtractionFailedException.emptyResponse();
        }

        // 4. 코드블록(``` ... ```) 제거
        result = result.trim();
        if (result.startsWith("```")) {
            int firstBrace = result.indexOf("{");
            int lastBrace = result.lastIndexOf("}");
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                result = result.substring(firstBrace, lastBrace + 1).trim();
                log.debug("[LlmExtractionService] 코드블록 제거 후 응답: {}", result);
            }
        }

        // 5. JSON 형식 검증
        if (!result.startsWith("{") || !result.endsWith("}")) {
            log.error("[LlmExtractionService] LLM 응답이 JSON이 아님: {}", result);
            throw ClothesExtractionFailedException.invalidJson();
        }

        // 6. JSON 파싱
        try {
            ClothesExtraInfo parsed = objectMapper.readValue(result, ClothesExtraInfo.class);
            log.info("[LlmExtractionService] JSON 파싱 성공: name={}, typeRaw={}, attributes={}",
                parsed.name(), parsed.typeRaw(), parsed.attributes());
            return parsed;
        } catch (Exception e) {
            log.error("[LlmExtractionService] JSON 파싱 실패: {}", result, e);
            throw ClothesExtractionFailedException.parsingError(e);
        }
    }

    /** LLM에 넘길 입력 데이터 */
    private String buildLlmInput(BasicClothesInfo basicInfo) {
        String inputString = """
            [상품명] %s
            [본문] %s
            """.formatted(
            safe(basicInfo.name()),
            safe(basicInfo.bodyText())
        );
        log.info("[LlmExtractionService] llmInput: {}", inputString);
        return inputString;
    }

    /** LLM 프롬프트(지시문 + JSON 형식 + 분석 텍스트) 생성 */
    private Prompt buildPrompt(String llmInput) {
        return new Prompt(new UserMessage("""
                반드시 아래 JSON 형식만 출력하세요. 
            다른 텍스트나 코드 블록은 절대 포함하지 마세요.
            
            [규칙]
            - "본문(bodyText)"을 최우선 근거로 추출, 없으면 상품명(name) 참고
            - 계절은 소재/두께 기준으로 분류:
                * 다운/패딩/헤비 아우터 → 겨울
                * 얇은 아우터(자켓, 가디건 등) → 봄/가을
                * 반팔/얇은 소재 → 여름
                * 정장/언더웨어/기본 티셔츠 등 → 사계절
            - 값이 없으면 반드시 "" (빈 문자열)로 출력
            
            [출력 형식]
            {
              "name": "제품명",
              "typeRaw": "상의",
              "attributes": {
                "계절": "봄 | 여름 | 가을 | 겨울 | 봄/가을 | 사계절 | 기타",
                "색상": "",
                "소재": "",
                "스타일": "",
                "핏": ""
              }
            }
            
            분석할 텍스트:
            %s
            """.formatted(llmInput)));

    }

    private String safe(String value) {
        return (value == null) ? "" : value;
    }

}
