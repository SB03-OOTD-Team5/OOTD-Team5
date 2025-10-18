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
 * - 날씨 기반 (온도, 강수, 풍속, 습도)
 * - 색상, 소재, 계절, 두께 기반
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
        double shoesScore = c.shoesType() != null ? c.shoesType().getWeatherScore(info) : 0;
        double outerScore = (c.type() == ClothesType.OUTER && c.outerType() != null)
            ? c.outerType().getWeatherScore(info) : 0;
        double topScore = (c.type() == ClothesType.TOP && c.topType() != null)
            ? c.topType().getWeatherScore(info) : 0;
        double bottomScore = (c.type() == ClothesType.BOTTOM && c.bottomType() != null)
            ? c.bottomType().getWeatherScore(info) : 0;
        double materialScore = c.material() != null ? c.material().getWeatherScore(info) : 0;

        double weighted = (shoesScore + outerScore + topScore + bottomScore + materialScore) / 6.0;

        // 평균 기준점
        double base = 50.0;
        double score = Math.max(40, Math.min(60, base + weighted));

        StringBuilder sb = new StringBuilder();
        if (shoesScore != 0) sb.append(String.format("신발(%+.1f) ", shoesScore));
        if (outerScore != 0) sb.append(String.format("아우터(%+.1f) ", outerScore));
        if (topScore != 0) sb.append(String.format("상의(%+.1f) ", topScore));
        if (bottomScore != 0) sb.append(String.format("하의(%+.1f) ", bottomScore));
        if (materialScore != 0) sb.append(String.format("소재(%+.1f) ", materialScore));

        if (sb.length() > 0) {
            log.debug("[SingleItemScoringEngine] '{}' ({}) 날씨 기반 적용 점수: {}→ 총점 {}",
                c.name(), c.type(), sb.toString().trim(), String.format("%.1f", score));
        }
        return score;
    }
}
