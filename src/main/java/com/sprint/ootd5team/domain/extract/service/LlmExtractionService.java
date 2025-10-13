package com.sprint.ootd5team.domain.extract.service;

import com.sprint.ootd5team.base.exception.clothes.ClothesExtractionFailedException;
import com.sprint.ootd5team.base.llm.LlmJsonClient;
import com.sprint.ootd5team.base.llm.LlmProvider;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.extract.dto.BasicClothesInfo;
import com.sprint.ootd5team.domain.extract.dto.ClothesExtraInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    private final LlmJsonClient llmJsonClient;

    public ClothesExtraInfo extractExtra(BasicClothesInfo basic, Map<String, ClothesAttribute> attributeCache) {
        String llmInput = buildLlmInput(basic);
        String candidateList = buildCandidateList(attributeCache);

        Prompt prompt = buildPrompt(llmInput, candidateList);

        return llmJsonClient.callJsonPrompt(prompt, ClothesExtraInfo.class);
    }

    /** LLM에 넘길 입력 데이터 */
    private String buildLlmInput(BasicClothesInfo basicInfo) {
        String name = sanitizeInput(basicInfo.name(), 100);
        String body = sanitizeInput(basicInfo.bodyText(), 2000);

        String inputString = """
            <user_input>
            [상품명] %s
            [본문] %s
            </user_input>
            """.formatted(name, body);

        log.debug("[LlmExtractionService] llmInput length={}, hash={}", inputString.length(),
            inputString.hashCode());

        return inputString;
    }

    private String buildCandidateList(Map<String, ClothesAttribute> attributeCache) {
        return attributeCache.values().stream()
            .map(attr -> "- " + attr.getName() + ": " +
                attr.getDefs().stream()
                    .map(def -> def.getAttDef())
                    .collect(Collectors.joining(", ", "[", "]"))
            )
            .collect(Collectors.joining("\n"));
    }

    /** LLM 프롬프트(지시문 + JSON 형식 + 분석 텍스트) 생성 */
    private Prompt buildPrompt(String llmInput, String candidateList) {
        return new Prompt(new UserMessage("""
            <instruction>
            반드시 아래 JSON 형식만 출력하세요.
            다른 텍스트나 코드 블록은 절대 포함하지 마세요.
    
            [규칙]
            - "본문(bodyText)"을 최우선 근거로 추출, 없으면 상품명(name) 참고
            - 후보 목록에 없는 값은 "기타"로 출력하세요
            - 값이 없으면 반드시 "" (빈 문자열)로 출력
            </instruction>
            
            <expected_output>
            {
              "name": "제품명",
              "typeRaw": "상의",
              "attributes": {
                "속성1" : ""
              }
            }
            
            [속성 후보 목록]
            %s

            </expected_output>
            
            %s
            """.formatted(candidateList, llmInput)));
    }

    /** 입력 문자열 검증 + 길이 제한 */
    private String sanitizeInput(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        String sanitized = input.trim();

        // 의심 키워드 검증
        String lower = sanitized.toLowerCase();
        List<String> forbidden = List.of("ignore previous", "output format", "system prompt");
        for (String bad : forbidden) {
            if (lower.contains(bad)) {
                log.warn("[LlmExtractionService] 의심 키워드 감지: {}", bad);
                throw ClothesExtractionFailedException.invalidJson();
            }
        }

        // 길이 제한
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength) + "...";
        }
        return sanitized;
    }

}
