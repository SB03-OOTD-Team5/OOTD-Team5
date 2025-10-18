package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;

/**
 * 아우터 종류 Enum
 * - 문자열 기반 자동 분류
 * - 날씨(온도, 강수, 풍속) 기반 점수 계산
 */
public enum OuterType {
    CARDIGAN("가디건", new String[]{"cardigan"}),
    COAT("코트", new String[]{"coat"}),
    TRENCH_COAT("트렌치코트", new String[]{"트렌치", "trench", "trench coat"}),
    PADDING("패딩", new String[]{"다운", "점퍼", "padded", "패딩점퍼", "롱패딩"}),
    JACKET("자켓", new String[]{"재킷", "블레이저", "jacket"}),
    HOODED_JACKET("후드집업", new String[]{"hood zip", "후드집업", "후드집"}),
    OTHER("기타", new String[]{});

    private final String displayName;
    private final String[] aliases;

    OuterType(String displayName, String[] aliases) {
        this.displayName = displayName;
        this.aliases = aliases;
    }

    /** 날씨 기반 점수 계산 */
    public double getWeatherScore(RecommendationInfoDto info) {
        double score = 0.0;

        double feels = info.personalFeelsTemp();

        WeatherInfoDto w = info.weatherInfo();
        WindspeedLevel wind = w.windspeedLevel();
        PrecipitationType precip = w.precipitationType();

        switch (this) {
            case CARDIGAN -> {
                // 간절기용 (봄·가을)
                if (feels >= 14 && feels <= 22) score += 3.5;
                else if (feels >= 10 && feels < 14) score += 2;
                else if (feels < 8) score -= 2;
                if (wind == WindspeedLevel.STRONG) score -= 1;
            }

            case JACKET -> {
                // 봄·가을 초입 (바람 불 때 안정적)
                if (feels >= 10 && feels <= 18) score += 4;
                else if (feels >= 7 && feels < 10) score += 2.5;
                else if (feels > 20) score -= 1.5;
                if (wind == WindspeedLevel.STRONG) score += 0.5;
            }

            case TRENCH_COAT -> {
                // 비 오는 날, 봄·가을 초입
                if (feels >= 9 && feels <= 18) score += 4.5;
                else if (feels >= 7 && feels < 9) score += 2;
                else if (feels > 22) score -= 2;
                if (wind == WindspeedLevel.STRONG) score += 0.5;
            }

            case COAT -> {
                // 늦가을~겨울
                if (feels <= 10 && feels >= 3) score += 4;
                else if (feels <= 2) score += 5;
                else if (feels > 15) score -= 2;
                if (precip.isSnowy()) score += 2;
                if (wind == WindspeedLevel.STRONG) score += 1;
            }

            case PADDING -> {
                // 한겨울
                if (feels < 0) score += 5;
                else if (feels <= 5) score += 4;
                else if (feels <= 10) score += 2;
                else score -= 3;
                if (precip.isSnowy() || precip.isRainy()) score += 1.5;
                if (wind == WindspeedLevel.STRONG) score += 1.5;
            }

            case HOODED_JACKET -> {
                // 봄·가을 / 약간 쌀쌀할 때
                if (feels >= 10 && feels <= 18) score += 3.5;
                else if (feels < 8) score += 1;
                else if (feels > 22) score -= 2;
                if (precip.isRainy()) score += 0.5;
                if (wind == WindspeedLevel.STRONG) score += 0.5;
            }

            default -> score += 0.0;
        }

        if (info.profileInfo().temperatureSensitivity() <= 3) {
            score += 1.0;
        }

        // 점수 제한 (-3 ~ +5)
        return Math.max(-3, Math.min(5, score));
    }
}
