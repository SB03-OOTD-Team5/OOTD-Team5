package com.sprint.ootd5team.domain.clothes.enums;

import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import java.util.regex.Pattern;

public enum ClothesType {
    TOP,
    BOTTOM,
    DRESS,
    OUTER,
    UNDERWEAR,
    ACCESSORY,
    SHOES,
    SOCKS,
    HAT,
    BAG,
    SCARF,
    ETC;

    /**
     * 주어진 문자열을 Enum 값으로 정규화
     * LLM이나 외부 입력에서 들어온 표현을 프로젝트 표준 값으로 매핑
     * - 부분 일치(contains) 허용 → "크롭 티셔츠"도 TOP으로 매핑
     * - 매칭 실패 시 ETC 반환
     */
    public static ClothesType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return ClothesType.ETC;
        }
        String normalized = raw.trim().toLowerCase();

        if (matches(normalized, "상의|top|shirt|t-?shirt|tee")) {
            return ClothesType.TOP;
        }
        if (matches(normalized, "하의|바지|치마|bottom|pants?|jeans?|skirt")) {
            return ClothesType.BOTTOM;
        }
        if (matches(normalized, "원피스|드레스|dress|one[- ]?piece")) {
            return ClothesType.DRESS;
        }
        if (matches(normalized, "아우터|코트|자켓|outer|jacket|coat")) {
            return ClothesType.OUTER;
        }
        if (matches(normalized, "속옷|underwear|innerwear")) {
            return ClothesType.UNDERWEAR;
        }
        if (matches(normalized, "악세사리|액세서리|accessor(y|ies)")) {
            return ClothesType.ACCESSORY;
        }
        if (matches(normalized, "신발|운동화|부츠|shoes?|sneakers?|boots?")) {
            return ClothesType.SHOES;
        }
        if (matches(normalized, "양말|socks?")) {
            return ClothesType.SOCKS;
        }
        if (matches(normalized, "모자|hat|cap|beanie")) {
            return ClothesType.HAT;
        }
        if (matches(normalized, "가방|bag|backpack|handbag")) {
            return ClothesType.BAG;
        }
        if (matches(normalized, "스카프|목도리|scarf")) {
            return ClothesType.SCARF;
        }

        return ClothesType.ETC;
    }

    /**
     * 정규식 매칭 헬퍼
     * - 단어 경계(\b) 포함해서 토큰 단위로 매칭
     */
    private static boolean matches(String input, String regex) {
        return Pattern.compile("(" + regex + ")",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS)
            .matcher(input)
            .find();
    }

    public double getWeatherScore(WeatherInfoDto weatherInfoDto) {
        double score = 0.0;

        double temperature = weatherInfoDto.temperature();
        PrecipitationType precip = weatherInfoDto.precipitationType();
        WindspeedLevel level = weatherInfoDto.windSpeedLevel();

        switch (this) {
            case OUTER, SCARF -> {
                if (precip.isSnowy()) {
                    score += 3;
                }
                if (temperature < 16) {
                    score += 2;
                }
                if (temperature < 4) {
                    score += 5;
                }
                if (WindspeedLevel.STRONG.equals(level)) {
                    score += 5;
                }
            }
        }

        return score;
    }
}
