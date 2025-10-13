package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.recommendation.engine.model.ClothesScore;
import com.sprint.ootd5team.domain.recommendation.enums.Color;
import com.sprint.ootd5team.domain.recommendation.enums.Material;
import com.sprint.ootd5team.domain.recommendation.enums.type.BottomType;
import com.sprint.ootd5team.domain.recommendation.enums.type.OuterType;
import com.sprint.ootd5team.domain.recommendation.enums.type.ShoesType;
import com.sprint.ootd5team.domain.recommendation.enums.type.TopType;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationInfoMapper;
import com.sprint.ootd5team.domain.weather.entity.Weather;
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

    private final RecommendationInfoMapper recommendationInfoMapper;

    /**
     * 개별 의상 점수 계산
     */
    public double calculateSingleItemScore(
        ClothesFilteredDto clothes,
        WeatherInfoDto weather
    ) {
        double shoesScore = scoreByShoes(clothes, weather);
        double outerScore = scoreByOuter(clothes, weather);
        double topScore = scoreByTop(clothes, weather);
        double bottomScore = scoreByBottom(clothes, weather);
        double colorScore = scoreByColor(clothes, weather);
        double typeScore = scoreByType(clothes, weather);
        double materialScore = scoreByMaterial(clothes, weather);

        double weightedScore =
            ((shoesScore + outerScore + topScore + bottomScore + colorScore + typeScore
                + materialScore) / 7.0);

        // === 정규화 ===
        // 점수 폭이 너무 크지 않도록 보정 (ex: -10~+10 사이)
        if (weightedScore > 10) {
            weightedScore = 10;
        }
        if (weightedScore < -10) {
            weightedScore = -10;
        }

        // === 총점 계산 ===
        double base = 50.0;
        double score = base + weightedScore;

        if (score != base) {
            double delta = score - base;
            log.debug("[SingleItemScoringEngine] '{}' ({})총점: {} ({}{}, {})",
                clothes.name(), clothes.type(),
                String.format("%.1f", score),
                delta > 0 ? "+" : "", String.format("%.1f", delta),
                delta > 0 ? "상승" : "하락"
            );
        }
        return score;
    }

    /**
     * 타입별 상위 N개 반환
     */
    public List<ClothesScore> getTopItemsByType(
        List<ClothesFilteredDto> clothes,
        Weather weather,
        int limit
    ) {
        WeatherInfoDto weatherDto = recommendationInfoMapper.toWeatherInfoDto(weather);

        return clothes.stream()
            .collect(Collectors.groupingBy(ClothesFilteredDto::type))
            .entrySet().stream()
            .flatMap(entry -> {
                ClothesType type = entry.getKey();
                List<ClothesFilteredDto> group = entry.getValue();

                List<ClothesScore> top = getTopN(group, limit, weatherDto);

                log.debug("[SingleItemScoringEngine] 타입 {} → 상위 {}개 선정 (총 {}개 중)\n",
                    type, top.size(), group.size());
                return top.stream();
            })
            .toList();
    }

    private List<ClothesScore> getTopN(List<ClothesFilteredDto> group, int limit,
        WeatherInfoDto weatherDto) {
        PriorityQueue<ClothesScore> minHeap =
            new PriorityQueue<>(Comparator.comparingDouble(ClothesScore::score));

        for (ClothesFilteredDto c : group) {
            double score = calculateSingleItemScore(c, weatherDto);
            ClothesScore clothesScore = ClothesScore.from(c, score);

            minHeap.offer(clothesScore);
            if (minHeap.size() > limit) {
                minHeap.poll();
            }
        }

        return minHeap.stream()
            .sorted(Comparator.comparingDouble(ClothesScore::score).reversed())
            .toList();
    }

    /* -------------------------------- 점수 계산 로직 -------------------------------- */

    /** 날씨 기반 신발 보너스 */
    private double scoreByShoes(ClothesFilteredDto c, WeatherInfoDto w) {
        // 속성 → 이름 순서로 신발 종류 유추
        String attrValue = getAttr(c, "신발 종류");
        ShoesType shoesType = !attrValue.isBlank()
            ? ShoesType.fromString(attrValue)
            : ShoesType.fromString(c.name());

        if (shoesType == ShoesType.OTHER) {
            log.trace("[SingleItemScoringEngine] '{}' 신발 종류 미확인", c.name());
            return 0;
        }

        double score = shoesType.getWeatherScore(w);

        if (score != 0) {
            logScore("날씨기반 신발", c, score,
                String.format("type=%s", shoesType.name()));
        }

        return score;
    }

    /** 날씨 기반 아우터 보너스 */
    private double scoreByOuter(ClothesFilteredDto c, WeatherInfoDto w) {
        if (c.type() != ClothesType.OUTER) {
            return 0;
        }

        String attrValue = getAttr(c, "아우터 종류");
        OuterType outerType = !attrValue.isBlank()
            ? OuterType.fromString(attrValue)
            : OuterType.fromString(c.name());

        if (outerType == OuterType.OTHER) {
            return 0;
        }

        double score = outerType.getWeatherScore(w);

        if (score != 0) {
            logScore("날씨기반 아우터", c, score,
                String.format("outer='%s'", outerType.name()));
        }

        return score;
    }

    /** 날씨 기반 상의 보너스 */
    private double scoreByTop(ClothesFilteredDto c, WeatherInfoDto w) {
        if (c.type() != ClothesType.TOP) {
            return 0;
        }

        String attrValue = getAttr(c, "상의 종류");
        TopType type = !attrValue.isBlank()
            ? TopType.fromString(attrValue)
            : TopType.fromString(c.name());

        if (type == TopType.OTHER) {
            return 0;
        }

        double score = type.getWeatherScore(w);
        if (score != 0) {
            logScore("날씨기반 상의", c, score, String.format("top='%s'", type.name()));
        }
        return score;
    }

    /** 날씨 기반 하의 보너스 */
    private double scoreByBottom(ClothesFilteredDto c, WeatherInfoDto w) {
        if (c.type() != ClothesType.BOTTOM) {
            return 0;
        }

        String attrValue = getAttr(c, "하의 종류");
        BottomType type = !attrValue.isBlank()
            ? BottomType.fromString(attrValue)
            : BottomType.fromString(c.name());

        if (type == BottomType.OTHER) {
            return 0;
        }

        double score = type.getWeatherScore(w);
        if (score != 0) {
            logScore("날씨기반 하의", c, score, String.format("bottom='%s'", type.name()));
        }
        return score;
    }

    /** 날씨 기반 색상 보너스 */
    private double scoreByColor(ClothesFilteredDto c, WeatherInfoDto w) {
        String value = getAttr(c, "색상");
        if (value.isBlank()) {
            return 0;
        }

        Color color = Color.fromString(value);
        double score = color.getWeatherScore(w);

        if (score != 0) {
            logScore("날씨기반 색상", c, score,
                String.format("color='%s', tone=%s", color.displayName(), color.tone()));
        }

        return score;
    }

    /** 날씨 기반 의상 타입 보너스 */
    private double scoreByType(ClothesFilteredDto c, WeatherInfoDto w) {
        ClothesType type = c.type();
        double score = type.getWeatherScore(w);

        if (score != 0) {
            logScore("날씨기반 타입", c, score,
                String.format("type='%s'", type));
        }
        return score;
    }

    /** 날씨 기반 의상 소재 보너스 */
    private double scoreByMaterial(ClothesFilteredDto c, WeatherInfoDto w) {
        String value = getAttr(c, "소재");
        if (value.isBlank()) {
            return 0;
        }

        Material material = Material.fromString(value);
        double score = material.getWeatherScore(w);

        if (score != 0) {
            logScore("날씨기반 소재", c, score, String.format("material='%s'", value));
        }
        return score;
    }

    /**
     * DTO에서 속성값 조회
     */
    private String getAttr(ClothesFilteredDto c, String key) {
        String val = c.attributes().stream()
            .filter(a -> a.definitionName().equals(key))
            .map(ClothesAttributeWithDefDto::value)
            .findFirst()
            .orElse("");

        if (val.isEmpty()) {
            log.trace("[SingleItemScoringEngine] clothesId={}, name={}, key='{}' 속성 없음",
                c.clothesId(), c.name(), key);
        }

        return val;
    }

    /**
     * 점수 로그 출력 공통 메서드
     */
    private void logScore(String category, ClothesFilteredDto c, double score, String detail) {
        if (score == 0) {
            return;
        }

        String action = score > 0 ? "보너스" : "감점";
        log.debug("[SingleItemScoringEngine] {}점수: name='{}', {} → {} {}점",
            category, c.name(), detail, action, score);
    }
}
