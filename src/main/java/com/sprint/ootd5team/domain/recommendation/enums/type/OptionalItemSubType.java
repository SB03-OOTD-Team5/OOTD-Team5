package com.sprint.ootd5team.domain.recommendation.enums.type;

import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;

/**
 * 손수건/우산/마스크/장갑/양산/부채 등 옵션 아이템
 * - keywords: 이름/속성 파싱용
 * - shouldInclude(info): 특정 조건에서 무조건 포함
 */
public enum OptionalItemSubType {
    UMBRELLA("우산", new String[]{"umbrella", "레인코트", "raincoat"}),
    SUNSHADE("양산", new String[]{"sunshade", "parasol"}),
    FAN("부채", new String[]{"fan", "휴대용선풍기", "handyfan"}),
    HANDKERCHIEF("손수건", new String[]{"handkerchief", "땀수건"}),
    GLOVES("장갑", new String[]{"gloves"}),
    NONE("없음", new String[]{});

    private final String displayName;
    private final String[] aliases;

    OptionalItemSubType(String displayName, String[] aliases) {
        this.displayName = displayName;
        this.aliases = aliases;
    }

    /** 날씨 기반 포함 여부 (true/false) */
    public boolean shouldInclude(RecommendationInfoDto info) {
        if (info == null || info.weatherInfo() == null || this == NONE) {
            return false;
        }

        double feels = info.personalFeelsTemp();
        WeatherInfoDto w = info.weatherInfo();
        PrecipitationType precip = w.precipitationType();
        double rainProbRaw = w.precipitationProbability();
        double rainProb = Math.max(0.0, Math.min(1.0, rainProbRaw));
        WindspeedLevel wind = w.windspeedLevel();

        boolean isRainy = precip != null && precip.isRainy();
        boolean isSnowy = precip != null && precip.isSnowy();
        boolean isClear = precip != null && precip.isClear();

        return switch (this) {
            case UMBRELLA -> isRainy || isSnowy || rainProb >= 0.6;

            case SUNSHADE -> isClear && rainProb < 0.2 && feels >= 24
                && wind != WindspeedLevel.STRONG;

            case FAN -> feels >= 27 && isClear && rainProb < 0.2
                && wind != WindspeedLevel.STRONG;

            case HANDKERCHIEF -> feels >= 24;

            case GLOVES -> feels <= 7 || isSnowy;

            default -> false;
        };
    }

}
