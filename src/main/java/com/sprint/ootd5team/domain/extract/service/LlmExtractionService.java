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
            [URL] %s
            """.formatted(
            safe(basicInfo.name()),
            safe(basicInfo.bodyText()),
            safe(basicInfo.url())
        );
        log.info("[WebClothesExtractor] llmInput: {}", inputString);
        return inputString;
    }

    /** LLM 프롬프트(지시문 + JSON 형식 + 분석 텍스트) 생성 */
    private Prompt buildPrompt(String llmInput) {
        return new Prompt(new UserMessage("""
            반드시 아래 형식의 JSON만 출력하세요. 
            절대로 빈 JSON이나 다른 텍스트를 추가하지 마세요.
            코드 블록(```json ... ```)도 사용하지 마세요.
            아래 JSON 예시는 형식만 보여주는 것이며, 값은 그대로 복사하지 마세요.
            (선택지 중 하나)라는 안내가 있으면 []안의 값 중 하나로 반환하세요. 
            (선택지 중 하나)에서 반환할 수 있는 값이 없다면 "기타"로 반환하세요.
            
            [중요 규칙]
                  - 의상 속성은 반드시 "본문(bodyText)" 내용을 최우선으로 근거하여 추출합니다.
                  - 본문에 정보가 없을 경우에만 URL, 상품명(name)을 보조적으로 참고합니다.
                  - URL은 브랜드명, 상품명, 카테고리와 관련된 단서를 제공할 수 있습니다.
            
                  계절은 실제 착용 가능한 시기에 맞게 엄격히 분류하고,
                  소재/두께/보온성에 근거해 판단하세요.  
                    * 다운/패딩/헤비 아우터 → 겨울
                    * 얇은 아우터(자켓, 가디건 등) → 봄/가을
                    * 반팔, 얇은 소재 → 여름
                    * 정장, 언더웨어, 기본 티셔츠 등 계절과 무관한 경우만 사계절
                    
            값을 찾을 수 없으면 예시의 값을 채우는 것이 아닌 반드시 ""빈 응답을 출력하세요.
            
            {
                 "name": "예시 제품명",
                 "typeRaw": "아우터",
                 "attributes": {
                   "계절": "(선택지 중 하나) [봄, 여름, 가을, 겨울, 봄/가을, 기타, 사계절]",
                   "색상": "블랙",
                   "소재": "데님",
                   "스타일": "캐주얼",
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
