package com.sprint.ootd5team.domain.recommendation.engine;

import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
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
        RecommendationClothesDto clothes,
        WeatherInfoDto weather
    ) {
        double score = 50.0;
        score += scoreByShoes(clothes, weather);
        score += scoreByClothes(clothes, weather);
        score += scoreByColor(clothes, weather);
        score += scoreByWind(clothes, weather);
        score += scoreByHumidity(clothes, weather);
        score += scoreByRainMaterial(clothes, weather);

        log.debug("[SCORE] clothesId={}, type={}, total={}", clothes.clothesId(), clothes.type(),
            score);
        return score;
    }

    /**
     * 타입별 상위 N개 반환
     */
    public List<ScoredClothes> getTopItemsByType(
        List<RecommendationClothesDto> clothes,
        Weather weather,
        int limit
    ) {
        log.debug("[getTopItemsByType] 전체 의상 수={}, limit={}", clothes.size(), limit);

        var grouped = clothes.stream()
            .collect(Collectors.groupingBy(RecommendationClothesDto::type));

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

        log.debug("[getTopItemsByType] 결과 의상 수={}", result.size());
        return result;
    }

    /* -------------------------------- 점수 계산 로직 -------------------------------- */

    /** 신발: 날씨(비/눈/계절) 기반 */
    private double scoreByShoes(RecommendationClothesDto c, WeatherInfoDto w) {
        double score = 0;
        PrecipitationType pType = w.precipitationType();
        String shoeType = getAttr(c, "신발 종류");

        if ((pType.isRainy() || pType.isSnowy()) && shoeType.contains("장화")) {
            score += 10;
        }

        if (score != 0) {
            log.debug("[scoreByShoes] {}: {} +{}", c.clothesId(), shoeType, score);
        }
        return score;
    }

    /** 온도 / 두께 / 계절 기반 */
    private double scoreByClothes(RecommendationClothesDto c, WeatherInfoDto w) {
        double score = 0;
        double temp = w.currentTemperature();
        String thickness = getAttr(c, "두께");

        // 온도 대비 두께
        if (temp < 10 && thickness.contains("두꺼움")) {
            score += 3;
        }
        if (temp > 25 && thickness.contains("얇음")) {
            score += 2;
        }

        // 아우터
        if (temp < 20 && c.type() == ClothesType.OUTER) {
            score += 5;
        }

        if (score != 0) {
            log.debug("[scoreByClothes] {}: temp={}, thickness={}, +{}",
                c.clothesId(), temp, thickness, score);
        }
        return score;
    }

    /** 색상 기반 점수 */
    private double scoreByColor(RecommendationClothesDto c, WeatherInfoDto w) {
        double score = 0;
        PrecipitationType pType = w.precipitationType();
        String colorName = getAttr(c, "색상");
        ColorTone tone = ColorTone.fromColorName(colorName);

        if (pType.isClear() && tone.isBright(colorName)) {
            score += 2;
        }
        if (!pType.isClear() && tone.isBright(colorName)) {
            score -= 2;
        }
        if (pType.isRainy() && tone.isDark(colorName)) {
            score += 1;
        }

        if (score != 0) {
            log.debug("[scoreByColor] {}: color={}, tone={}, pType={}, +{}",
                c.clothesId(), colorName, tone, pType, score);
        }
        return score;
    }

    /** 바람 세기 기반 (얇은 옷 감점, 두꺼운 옷 보너스) */
    private double scoreByWind(RecommendationClothesDto c, WeatherInfoDto w) {
        double score = 0;
        WindspeedLevel wind = w.windSpeedLevel();
        String thickness = getAttr(c, "두께");

        if (wind == WindspeedLevel.STRONG && thickness.contains("얇음")) {
            score -= 3;
        }
        if (wind == WindspeedLevel.STRONG && thickness.contains("두꺼움")) {
            score += 2;
        }

        if (score != 0) {
            log.debug("[scoreByWind] {}: wind={}, thickness={}, +{}", c.clothesId(), wind,
                thickness, score);
        }
        return score;
    }

    /** 습도 기반 (통풍 소재 보너스, 땀 배출 어려운 소재 감점) */
    private double scoreByHumidity(RecommendationClothesDto c, WeatherInfoDto w) {
        double score = 0;
        double humidity = w.currentHumidity();
        String material = getAttr(c, "소재");

        if (humidity > 70 && (material.contains("리넨") || material.contains("면"))) {
            score += 3;
        }
        if (humidity > 70 && material.contains("폴리에스터")) {
            score -= 2;
        }

        if (score != 0) {
            log.debug("[scoreByHumidity] {}: humidity={}, material={}, +{}", c.clothesId(),
                humidity, material, score);
        }
        return score;
    }

    /** 비/눈일 때 소재 기반 */
    private double scoreByRainMaterial(RecommendationClothesDto c, WeatherInfoDto w) {
        double score = 0;
        PrecipitationType pType = w.precipitationType();
        String material = getAttr(c, "소재");

        if (pType.isRainy() || pType.isSnowy()) {
            if (material.contains("나일론") || material.contains("폴리에스터")) {
                score += 2;
            }
            if (material.contains("면")) {
                score -= 2;
            }
        }

        if (score != 0) {
            log.debug("[scoreByRainMaterial] {}: precip={}, material={}, +{}", c.clothesId(), pType,
                material, score);
        }
        return score;
    }

    /**
     * DTO에서 속성값 조회
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
