package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 단일 의상 점수 계산 엔진
 * - 날씨 기반 소재점수, 세부 아이템 점수
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class SingleItemScoringEngine {

    private static final int MAX_ITEMS_PER_TYPE = 5;       // 타입별 의상 후보 개수

    /**
     * 타입별 상위 N개 반환
     */
    public List<ClothesScore> getTopItemsByType(
        RecommendationInfoDto info,
        List<ClothesFilteredDto> candidates
    ) {
        return candidates.stream()
            .collect(Collectors.groupingBy(ClothesFilteredDto::type))
            .entrySet().stream()
            .flatMap(entry -> {
                ClothesType type = entry.getKey();
                List<ClothesFilteredDto> group = entry.getValue();

                List<ClothesScore> top = getTopN(info, group);

                log.info("[SingleItemScoringEngine] 타입 {} → 상위 {}개 선정 (총 {}개 중)\n",
                    type, top.size(), group.size());
                return top.stream();
            })
            .toList();
    }

    private List<ClothesScore> getTopN(
        RecommendationInfoDto info,
        List<ClothesFilteredDto> candidates
    ) {
        PriorityQueue<ClothesScore> minHeap =
            new PriorityQueue<>(Comparator.comparingDouble(ClothesScore::score));

        for (ClothesFilteredDto c : candidates) {
            double score = calculateSingleItemScore(info, c);
            ClothesScore clothesScore = ClothesScore.from(c, score);

            minHeap.offer(clothesScore);
            if (minHeap.size() > MAX_ITEMS_PER_TYPE) {
                minHeap.poll();
            }
        }

        return minHeap.stream()
            .sorted(Comparator.comparingDouble(ClothesScore::score).reversed())
            .toList();
    }

    /** 단품 날씨 점수 계산 */
    public double calculateSingleItemScore(RecommendationInfoDto info, ClothesFilteredDto c) {
        ClothesType type = c.type();

        double shoesScore = (type == ClothesType.SHOES && c.shoesType() != null) ? c.shoesType()
            .getWeatherScore(info) : 0.0;
        double outerScore = (type == ClothesType.OUTER && c.outerType() != null) ? c.outerType()
            .getWeatherScore(info) : 0.0;
        double topScore = (type == ClothesType.TOP && c.topType() != null) ? c.topType()
            .getWeatherScore(info) : 0.0;
        double bottomScore = (type == ClothesType.BOTTOM && c.bottomType() != null) ? c.bottomType()
            .getWeatherScore(info) : 0.0;
        double materialScore = (c.material() != null) ? c.material().getWeatherScore(info) : 0.0;

        double sum = 0.0;
        int denom = 0;

        // 타입별 점수 합산
        switch (type) {
            case SHOES -> {sum += shoesScore; denom++;}
            case OUTER -> {sum += outerScore; denom++;}
            case TOP -> {sum += topScore; denom++;}
            case BOTTOM -> {sum += bottomScore; denom++;}
            default -> {}
        }

        if (c.material() != null) {sum += materialScore; denom++;}

        if (denom == 0) {denom = 1;}
        double weighted = sum / denom;

        // 평균 기준점
        double base = 50.0;
        double score = Math.max(45, Math.min(60, base + weighted));

        // 로그 빌드
        StringBuilder sb = new StringBuilder();
        if (shoesScore != 0) {sb.append(String.format("신발(%+.1f) ", shoesScore));}
        if (outerScore != 0) {sb.append(String.format("아우터(%+.1f) ", outerScore));}
        if (topScore != 0) {sb.append(String.format("상의(%+.1f) ", topScore));}
        if (bottomScore != 0) {sb.append(String.format("하의(%+.1f) ", bottomScore));}
        if (materialScore != 0) {sb.append(String.format("소재(%+.1f) ", materialScore));}

        if (!sb.isEmpty()) {
            log.debug("[SingleItemScoringEngine] '{}' ({}) 적용 점수: {}→ 총점 {}",
                c.name(), type, sb.toString().trim(), score);
        }
        return score;
    }
}
