package com.sprint.ootd5team.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.llm.LlmJsonClient;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

/**
 * LLM 기반 코디 추천 서비스
 * <p>
 * - 필터링된 의상 목록을 기반으로 LLM이 코디 조합을 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmRecommendationService {

    private final LlmJsonClient llmJsonClient;
    private final ObjectMapper objectMapper;

    public List<UUID> recommendOutfit(RecommendationInfoDto info,
        List<RecommendationClothesDto> clothes) {
        String llmInput = buildLlmInput(info, clothes);
        Prompt prompt = buildPrompt(llmInput);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) llmJsonClient.callJsonPrompt(prompt,
            Map.class);

        Object rawIds = map.get("ids");
        if (!(rawIds instanceof List<?> list)) {
            log.warn("[LlmRecommendationService] ids 필드가 List 형식이 아님: {}", rawIds);
            return List.of();
        }

        return list.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(UUID::fromString)
            .toList();
    }

    /** LLM에 넘길 입력 데이터 */
    private String buildLlmInput(RecommendationInfoDto info,
        List<RecommendationClothesDto> clothes) {
        try {
            // 옷 목록 샘플링 (최대 20개)
            List<RecommendationClothesDto> sampled = new ArrayList<>(clothes);
            Collections.shuffle(sampled);
            List<RecommendationClothesDto> limited = sampled.stream().limit(20).toList();

            // JSON 통합 구조 생성
            Map<String, Object> payload = Map.of(
                "profile", info.profileInfo(),
                "weather", info.weatherInfo(),
                "clothes", limited
            );

            // 직렬화
            return objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(payload);

        } catch (JsonProcessingException e) {
            log.error("[LlmRecommendationService] JSON 직렬화 실패", e);
            return "{}";
        }
    }

    /** LLM 프롬프트(지시문 + JSON 형식 + 분석 텍스트) 생성 */
    private Prompt buildPrompt(String llmInput) {
        return new Prompt(new UserMessage("""
            <instruction>
            당신은 패션 코디 전문가입니다.
            아래 JSON 데이터는 사용자 정보, 날씨 정보, 그리고 의상 목록을 포함합니다.
            
            [규칙]
            - 상의 + 하의 조합 또는 원피스를 포함한 코디를 추천하세요.
            - 의상 타입은 최대 6개까지 포함할 수 있습니다.
            - ClothesType이 ACCESSORY, UNDERWEAR, ETC라면 최대 3개,
              나머지 타입은 1개씩만 선택하세요.
            - 날씨 정보와 사용자의 온도 민감도를 적극 반영하세요.
            - 색상, 계절, 소재가 어울리는 조합을 우선하세요.
            
            반드시 아래 JSON 형식만 출력하세요.
            다른 텍스트나 코드 블록(```)은 절대 포함하지 마세요.
            
            <expected_output>
            {
              "ids": ["상의_id", "하의_id", "신발_id"]
            }
            </expected_output>
            </instruction>
            
            <input_data>
            %s
            </input_data>
            """.formatted(llmInput)));
    }
}