package com.sprint.ootd5team.domain.recommendation.enums;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Color {
    NAVY("네이비", new String[]{"navy"}, ColorTone.COOL),
    SKY_BLUE("스카이블루", new String[]{"skyblue", "하늘색"}, ColorTone.COOL),
    ORANGE("오렌지", new String[]{"orange"}, ColorTone.WARM),
    MINT("민트", new String[]{"mint"}, ColorTone.COOL),
    LAVENDER("라벤더", new String[]{"lavender"}, ColorTone.COOL),
    KHAKI("카키", new String[]{"khaki"}, ColorTone.WARM),
    WINE("와인", new String[]{"wine", "버건디"}, ColorTone.WARM),
    SILVER("실버", new String[]{"silver", "은색"}, ColorTone.NEUTRAL),
    GOLD("골드", new String[]{"gold", "금색"}, ColorTone.WARM),
    BROWN("브라운", new String[]{"brown", "갈색"}, ColorTone.WARM),
    GRAY("그레이", new String[]{"gray", "회색"}, ColorTone.NEUTRAL),
    WHITE("화이트", new String[]{"white", "흰색"}, ColorTone.NEUTRAL),
    BLACK("블랙", new String[]{"black", "검정"}, ColorTone.NEUTRAL),
    BLUE("블루", new String[]{"blue", "파랑"}, ColorTone.COOL),
    GREEN("그린", new String[]{"green", "초록"}, ColorTone.COOL),
    PINK("핑크", new String[]{"pink", "분홍"}, ColorTone.WARM),
    RED("레드", new String[]{"red", "빨강"}, ColorTone.WARM),
    BEIGE("베이지", new String[]{"beige"}, ColorTone.WARM),
    PURPLE("퍼플", new String[]{"purple", "보라"}, ColorTone.COOL),
    YELLOW("옐로우", new String[]{"yellow", "노랑"}, ColorTone.WARM),
    OTHER("기타", new String[]{}, ColorTone.OTHER);

    private final String displayName;
    private final String[] aliases;
    private final ColorTone tone;

    Color(String displayName, String[] aliases, ColorTone tone) {
        this.displayName = displayName;
        this.aliases = aliases;
        this.tone = tone;
    }

    public ColorTone tone() {
        return tone;
    }

    public String displayName() {
        return displayName;
    }

    /** 밝은 색상인지 여부 */
    public boolean isBright() {
        return switch (this) {
            case WHITE, BEIGE, SKY_BLUE, SILVER, PINK, YELLOW -> true;
            default -> false;
        };
    }

    /** 어두운 색상인지 여부 */
    public boolean isDark() {
        return switch (this) {
            case BLACK, NAVY, GRAY, BROWN, WINE, KHAKI -> true;
            default -> false;
        };
    }

    /** 날씨 기반 의상 단품 점수 */
    public double getWeatherScore(RecommendationInfoDto info) {
        double score = 0.0;
        double temp = info.personalFeelsTemp();

        WeatherInfoDto weather = info.weatherInfo();
        PrecipitationType precip = weather.precipitationType();
        double precipProb = weather.precipitationProbability();

        // 맑은 날 밝은 색 보너스
        if (precip.isClear() && isBright()) {
            score += 2.0;
        }

        // 비 확률 높으면 밝은 색 감점
        if (precipProb > 0.5 && isBright()) {
            score -= 1.5;
        }

        // 온도 기반 톤 보정
        if (temp < 10 && tone == ColorTone.WARM) {
            score += 2.0;
        } else if (temp > 25 && tone == ColorTone.COOL) {
            score += 2.0;
        }

        return score;
    }
}
