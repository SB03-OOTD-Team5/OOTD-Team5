package com.sprint.ootd5team.domain.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.llm.LlmJsonClient;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
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
        List<ClothesFilteredDto> clothes) {
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
        List<ClothesFilteredDto> clothes) {
        try {
            // 옷 목록 샘플링 (최대 20개)
            List<ClothesFilteredDto> sampled = new ArrayList<>(clothes);
            Collections.shuffle(sampled);
            List<ClothesFilteredDto> limited = sampled.stream().limit(20).toList();

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
            - 필수 조합은 무조건 추가하고, 나머지 타입은 선택적으로 추가하세요.
            - 필수 조합: 상의 타입 + 하의 타입 또는 원피스 타입
            - 조합에서 총 의상 타입은 최대 6개까지 포함할 수 있습니다.
            - ACCESSORY, UNDERWEAR, ETC 타입은,
              각 코디당 최대 3개까지만 포함 가능합니다.
            - 날씨(온도, 강수, 풍속, 습도)와 사용자의 온도 민감도를 반드시 반영하세요.
            - 색상, 계절, 소재의 조화를 고려하고,
              같은 계열 혹은 보색 대비가 어울리는 조합을 우선하세요.
            - 전체 코디는 최대 6개의 아이템으로 구성되어야 합니다.
            
            반드시 아래 JSON 형식만 출력하세요.
            다른 텍스트나 코드 블록(```)은 절대 포함하지 마세요.
            
            <expected_output>
            {
              "ids": ["id", "id", "id"]
            }
            </expected_output>
            </instruction>
            
            <input_data>
            %s
            </input_data>
            """.formatted(llmInput)));
    }
}