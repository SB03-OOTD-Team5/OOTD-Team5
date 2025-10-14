package com.sprint.ootd5team.domain.recommendation.dto;

import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;

/**
 * 추천 관련 날씨정보: 강수, 바람, 하늘상태, 체감온도
 */
public record WeatherInfoDto(
    // 강수 상태
    PrecipitationType precipitationType,
    // 강수 확률
    double precipitationProbability,

    // 바람 세기
    WindspeedLevel windSpeedLevel,

    //하늘 상태
    SkyStatus skyStatus,

    ApparentTemperatureDto apparentTemperature
) {

}
