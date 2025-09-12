package com.sprint.ootd5team.domain.weather.dto.data;

import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record WeatherDto(
    UUID id,
    UUID profileId,
    Instant forecastedAt,       // 예보 산출 시각
    Instant forecastAt,         // 예보 대상 시각
    SkyStatus skyStatus,
    BigDecimal latitude,
    BigDecimal longitude,
    Integer xCoord,
    Integer yCoord,
    String locationNames,
    PrecipitationType precipitationType,
    Double precipitationAmount,
    Double precipitationProbability,
    Double humidity,
    Double humidityCompared,
    Double temperature,
    Double temperatureCompared,
    Double temperatureMin,
    Double temperatureMax,
    Double windspeed,
    WindspeedLevel windspeedLevel,
    Instant createdAt
) {
}
