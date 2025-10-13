package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.recommendation.engine.model.ScoredClothes;
import com.sprint.ootd5team.domain.recommendation.enums.ColorTone;
import com.sprint.ootd5team.domain.recommendation.mapper.RecommendationInfoMapper;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import java.util.Comparator;
import java.util.List;
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
        double base = 50.0;
        double score = base;

        score += scoreByShoes(clothes, weather);
        score += scoreByClothes(clothes, weather);
        score += scoreByColor(clothes, weather);
        score += scoreByWind(clothes, weather);
        score += scoreByHumidity(clothes, weather);
        score += scoreByRainMaterial(clothes, weather);

        if (score != base) {
            double delta = score - base;
            String trend = delta > 0 ? "상승" : "하락";
            log.debug("[SingleItemScoringEngine] '{}' ({}): 총점 {} ({}{}, {})\n",
                clothes.name(), clothes.type(), String.format("%.1f", score),
                delta > 0 ? "+" : "", String.format("%.1f", delta), trend);
        }
        return score;
    }

    /**
     * 타입별 상위 N개 반환
     */
    public List<ScoredClothes> getTopItemsByType(
        List<ClothesFilteredDto> clothes,
        Weather weather,
        int limit
    ) {
        log.debug("[SingleItemScoringEngine] 전체 의상 수={}, limit={}", clothes.size(), limit);

        var grouped = clothes.stream()
            .collect(Collectors.groupingBy(ClothesFilteredDto::type));

        grouped.forEach((type, group) ->
            log.debug("  - 타입={}, 그룹 크기={}", type, group.size())
        );

        List<ScoredClothes> result = grouped.values().stream()
            .flatMap(group -> group.stream()
                // 의상별 점수 계산 후 ScoredClothes로 매핑
                .map(c -> new ScoredClothes(
                    c, calculateSingleItemScore(c,
                    recommendationInfoMapper.toWeatherInfoDto(weather))))
                // 점수 높은 순으로 정렬
                .sorted(Comparator.comparingDouble(ScoredClothes::score).reversed())
                // 상위 N개만 유지
                .limit(limit))
            .toList();

        log.debug("[SingleItemScoringEngine] 결과 의상 수={}", result.size());
        return result;
    }

    /* -------------------------------- 점수 계산 로직 -------------------------------- */

    /** 신발: 날씨(비/눈/계절) 기반 */
    private double scoreByShoes(ClothesFilteredDto c, WeatherInfoDto w) {
        double score = 0;
        PrecipitationType pType = w.precipitationType();
        String value = getAttr(c, "신발 종류");
        if (value.isBlank()) {
            return 0;
        }

        if ((pType.isRainy() || pType.isSnowy()) && value.contains("장화")) {
            score += 10;
        }

        if (score != 0) {
            logScore("날씨기반 신발", c, score,
                String.format("type='%s', weather=%s", value, w.precipitationType()));
        }
        return score;
    }

    /** 온도 / 두께 / 계절 기반 */
    private double scoreByClothes(ClothesFilteredDto c, WeatherInfoDto w) {
        double score = 0;
        double temp = w.currentTemperature();
        String value = getAttr(c, "두께");
        if (value.isBlank()) {
            return 0;
        }

        // 온도 대비 두께
        if (temp < 10 && value.contains("두꺼움")) {
            score += 3;
        }
        if (temp > 25 && value.contains("얇음")) {
            score += 2;
        }

        // 아우터
        if (temp < 20 && c.type() == ClothesType.OUTER) {
            score += 5;
        }

        if (score != 0) {
            logScore("온도기반 두께", c, score,
                String.format("temp=%.1f°C, thickness='%s'", w.currentTemperature(), value));
        }
        return score;
    }

    /** 색상 기반 점수 */
    private double scoreByColor(ClothesFilteredDto c, WeatherInfoDto w) {
        double score = 0;
        PrecipitationType pType = w.precipitationType();
        String value = getAttr(c, "색상");
        if (value.isBlank()) {
            return 0;
        }

        ColorTone tone = ColorTone.fromColorName(value);

        if (pType.isClear() && tone.isBright(value)) {
            score += 2;
        }
        if (!pType.isClear() && tone.isBright(value)) {
            score -= 2;
        }
        if (pType.isRainy() && tone.isDark(value)) {
            score += 1;
        }

        if (score != 0) {
            logScore("하늘상태기반 색상", c, score,
                String.format("color='%s', tone=%s, weather=%s", value, tone,
                    w.precipitationType()));
        }
        return score;
    }

    /** 바람 세기 기반 (얇은 옷 감점, 두꺼운 옷 보너스) */
    private double scoreByWind(ClothesFilteredDto c, WeatherInfoDto w) {
        double score = 0;
        WindspeedLevel wind = w.windSpeedLevel();
        String value = getAttr(c, "두께");
        if (value.isBlank()) {
            return 0;
        }

        if (wind == WindspeedLevel.STRONG && value.contains("얇음")) {
            score -= 3;
        }
        if (wind == WindspeedLevel.STRONG && value.contains("두꺼움")) {
            score += 2;
        }

        if (score != 0) {
            logScore("바람세기기반 두께", c, score,
                String.format("wind=%s, thickness='%s'", w.windSpeedLevel(), value));
        }
        return score;
    }

    /** 습도 기반 (통풍 소재 보너스, 땀 배출 어려운 소재 감점) */
    private double scoreByHumidity(ClothesFilteredDto c, WeatherInfoDto w) {
        double score = 0;
        double humidity = w.currentHumidity();
        String value = getAttr(c, "소재");
        if (value.isBlank()) {
            return 0;
        }

        if (humidity > 70 && (value.contains("리넨") || value.contains("면"))) {
            score += 3;
        }
        if (humidity > 70 && value.contains("폴리에스터")) {
            score -= 2;
        }

        if (score != 0) {
            logScore("습도기반 소재", c, score,
                String.format("humidity=%.1f%%, material='%s'", humidity, value));
        }
        return score;
    }

    /** 비/눈일 때 소재 기반 */
    private double scoreByRainMaterial(ClothesFilteredDto c, WeatherInfoDto w) {
        double score = 0;
        PrecipitationType pType = w.precipitationType();
        String value = getAttr(c, "소재");
        if (value.isBlank()) {
            return 0;
        }

        if (pType.isRainy() || pType.isSnowy()) {
            if (value.contains("나일론") || value.contains("폴리에스터")) {
                score += 2;
            }
            if (value.contains("면")) {
                score -= 2;
            }
        }

        if (score != 0) {
            logScore("강수기반 소재", c, score, String.format("precip=%s, material='%s'", pType, value));
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
