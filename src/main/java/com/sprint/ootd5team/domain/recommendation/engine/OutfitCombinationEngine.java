package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 코디 조합 점수 계산 엔진
 * - 색상, 소재, 스타일, 구조적 조화 등을 기반으로 전체 outfit 점수를 산출
 */
@Slf4j
@Component
public class OutfitCombinationEngine {

    /**
     * 코디 조합 점수 계산 (총합)
     */
    public double calculateCombinationBonus(List<ClothesFilteredDto> outfit) {
        // 코디 조합 구성 문자열 생성
        String outfitSummary = outfit.stream()
            .map(c -> String.format("%s(%s)", c.name(), c.type()))
            .collect(Collectors.joining(", "));

        log.debug("[OutfitCombinationEngine] 코디 조합: [{}]", outfitSummary);

        // 각 세부 점수 계산
        double colorScore = calculateColorHarmony(outfit);
        double materialScore = calculateMaterialCompatibility(outfit);
        double styleScore = calculateStyleHarmony(outfit);
        double structuralPenalty = structuralPenalty(outfit);

        double totalScore = colorScore + materialScore + styleScore + structuralPenalty;

        log.debug(
            "[OutfitCombinationEngine] size={}, color={}, material={}, style={}, penalty={}, total={}",
            outfit.size(), colorScore, materialScore, styleScore, structuralPenalty, totalScore);
        ;

        return totalScore;
    }

    /* ==================== 점수 계산 ==================== */

    /** 소재 조화도 평가 */
    private double calculateMaterialCompatibility(List<ClothesFilteredDto> outfit) {
        return compareAttributes(outfit, "소재", (a, b) -> {
            Material m1 = Material.fromString(a);
            Material m2 = Material.fromString(b);
            return m1.isCompatibleWith(m2) ? 2 : -2;
        }, "소재 조합");
    }

    /** 색상 조화도 평가 */
    private double calculateColorHarmony(List<ClothesFilteredDto> outfit) {
        return compareAttributes(outfit, "색상", (a, b) -> {
            ColorTone c1 = ColorTone.fromColorName(a);
            ColorTone c2 = ColorTone.fromColorName(b);
            return c1.isHarmoniousWith(c2) ? 1.5 : -1.5;
        }, "색상 조합");
    }

    /** 스타일 조화도 평가 */
    private double calculateStyleHarmony(List<ClothesFilteredDto> outfit) {
        return compareAttributes(outfit, "스타일", (a, b) -> {
            ClothesStyle s1 = ClothesStyle.fromString(a);
            ClothesStyle s2 = ClothesStyle.fromString(b);
            return s1.isHarmoniousWith(s2) ? 2 : -2;
        }, "스타일 조합");
    }

    /** 구조적(조합) 패널티 (보완 필요) */
    private double structuralPenalty(List<ClothesFilteredDto> outfit) {
        boolean hasOuter = outfit.stream()
            .anyMatch(c -> c.type().name().equals("OUTER"));

        // 속성 없는 아이템은 스킵
        boolean hasShorts = outfit.stream()
            .filter(c -> {
                String length = getAttr(c, "종류");
                if (length.isBlank()) {
                    log.trace("[OutfitCombinationEngine] {}: 속성 없음 → 스킵", c.name());
                    return false;
                }
                return true;
            })
            .anyMatch(c -> getAttr(c, "종류").contains("반바지"));

        double penalty = (hasOuter && hasShorts) ? -5 : 0;

        if (penalty != 0) {
            log.trace("[OutfitCombinationEngine] 구조 패널티 적용: hasOuter={}, hasShorts={}, penalty={}",
                hasOuter, hasShorts, penalty);
        }

        return penalty;
    }

    /**
     * 공통 속성 비교 로직 — 속성이 없으면 스킵
     */
    private double compareAttributes(
        List<ClothesFilteredDto> outfit,
        String key,
        AttributeComparator comparator,
        String label
    ) {
        double score = 0;

        for (int i = 0; i < outfit.size(); i++) {
            for (int j = i + 1; j < outfit.size(); j++) {
                String v1 = getAttr(outfit.get(i), key);
                String v2 = getAttr(outfit.get(j), key);
                if (skipIfBlank(v1, v2, key, outfit.get(i), outfit.get(j))) {
                    continue;
                }

                double delta = comparator.compare(v1, v2);
                score += delta;

                log.trace("[OutfitCombinationEngine] {} {}({}) ↔ {}({}) → {}",
                    label, outfit.get(i).name(), v1, outfit.get(j).name(), v2, score);
            }
        }

        log.trace("[OutfitCombinationEngine] {} totalScore={}", label, score);
        return score;
    }

    /**
     * 속성값이 비어있으면 스킵 로그 출력
     */
    private boolean skipIfBlank(String v1, String v2, String key,
        ClothesFilteredDto c1, ClothesFilteredDto c2) {
        if (v1.isBlank() || v2.isBlank()) {
            log.trace("[OutfitCombinationEngine] {} or {} → '{}' 속성 없음 → 비교 스킵",
                c1.name(), c2.name(), key);
            return true;
        }
        return false;
    }

    /**
     * DTO 속성 값 조회
     */
    private String getAttr(ClothesFilteredDto clothes, String key) {
        String val = clothes.attributes().stream()
            .filter(a -> a.definitionName().equals(key))
            .map(ClothesAttributeWithDefDto::value)
            .findFirst()
            .orElse("");

        if (val.isEmpty()) {
            log.trace("[OutfitCombinationEngine] {}: '{}' 속성 없음", clothes.name(), key);
        }

        return val;
    }

    @FunctionalInterface
    private interface AttributeComparator {

        double compare(String a, String b);
    }
}
