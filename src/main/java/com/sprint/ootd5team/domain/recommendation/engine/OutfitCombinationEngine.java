package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import com.sprint.ootd5team.domain.recommendation.enums.ClothesStyle;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OutfitCombinationEngine {

    /**
     * 코디 조합 보너스 점수 계산
     */
    public double calculateCombinationBonus(List<RecommendationClothesDto> outfit) {
        double color = colorHarmony(outfit);
        double material = materialCompatibility(outfit);
        double style = styleHarmony(outfit);
        double combinationPenalty = structuralPenalty(outfit);

        double total = color + material + style + combinationPenalty;

        log.debug(
            "[OutfitCombination] outfit.size={}, color={}, material={}, style={}, penalty={}, total={}",
            outfit.size(), color, material, style, combinationPenalty, total);

        return total;
    }

    /**
     * 소재 조화도 평가
     */
    private double materialCompatibility(List<RecommendationClothesDto> outfit) {
        double score = 0;
        for (int i = 0; i < outfit.size(); i++) {
            for (int j = i + 1; j < outfit.size(); j++) {
                var m1 = Material.fromString(getAttr(outfit.get(i), "소재"));
                var m2 = Material.fromString(getAttr(outfit.get(j), "소재"));

                double delta = m1.isCompatibleWith(m2) ? 3 : -5;
                score += delta;

                log.trace("[materialCompatibility] {}({}) ↔ {}({}) => {}",
                    outfit.get(i).clothesId(), m1,
                    outfit.get(j).clothesId(), m2,
                    delta);
            }
        }
        log.debug("[materialCompatibility] total={}", score);
        return score;
    }

    /**
     * 색상 조화도 평가
     */
    private double colorHarmony(List<RecommendationClothesDto> outfit) {
        double score = 0;
        for (int i = 0; i < outfit.size(); i++) {
            for (int j = i + 1; j < outfit.size(); j++) {
                var c1 = ColorTone.fromColorName(getAttr(outfit.get(i), "색상"));
                var c2 = ColorTone.fromColorName(getAttr(outfit.get(j), "색상"));

                double delta = c1.isHarmoniousWith(c2) ? 2 : -3;
                score += delta;

                log.trace("[colorHarmony] {}({}) ↔ {}({}) => {}",
                    outfit.get(i).clothesId(), c1,
                    outfit.get(j).clothesId(), c2,
                    delta);
            }
        }
        log.debug("[colorHarmony] total={}", score);
        return score;
    }

    /**
     * 스타일 조화도 평가 (스타일 속성 기반)
     */
    private double styleHarmony(List<RecommendationClothesDto> outfit) {
        double score = 0;

        for (int i = 0; i < outfit.size(); i++) {
            for (int j = i + 1; j < outfit.size(); j++) {
                var s1 = ClothesStyle.fromString(getAttr(outfit.get(i), "스타일"));
                var s2 = ClothesStyle.fromString(getAttr(outfit.get(j), "스타일"));

                double delta = s1.isHarmoniousWith(s2) ? 2.5 : -4;
                score += delta;

                log.trace("[styleHarmony] {}({}) ↔ {}({}) => {}",
                    outfit.get(i).clothesId(), s1,
                    outfit.get(j).clothesId(), s2,
                    delta);
            }
        }

        log.debug("[styleHarmony] total={}", score);
        return score;
    }

    /**
     * 구조적(조합) 패널티 — 예: 아우터 + 반바지 같은 비조화 조합 감점
     */
    private double structuralPenalty(List<RecommendationClothesDto> outfit) {
        boolean hasOuter = outfit.stream().anyMatch(c -> c.type().name().equals("OUTER"));
        boolean hasShorts = outfit.stream().anyMatch(c -> getAttr(c, "길이").contains("반바지"));

        double penalty = (hasOuter && hasShorts) ? -10 : 0;
        log.debug("[structuralPenalty] hasOuter={}, hasShorts={}, penalty={}", hasOuter, hasShorts,
            penalty);
        return penalty;
    }

    /**
     * DTO 속성 값 조회
     */
    private String getAttr(RecommendationClothesDto c, String key) {
        String val = c.attributes().stream()
            .filter(a -> a.definitionName().equals(key))
            .map(ClothesAttributeWithDefDto::value)
            .findFirst()
            .orElse("");

        if (val.isEmpty()) {
            log.trace("[getAttr] clothesId={}, key='{}' 속성 없음", c.clothesId(), key);
        }

        return val;
    }
}
