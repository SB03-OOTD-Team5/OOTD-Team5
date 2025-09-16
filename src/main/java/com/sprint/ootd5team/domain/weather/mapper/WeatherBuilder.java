package com.sprint.ootd5team.domain.weather.mapper;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import com.sprint.ootd5team.domain.weather.external.kma.KmaCategoryType;
import com.sprint.ootd5team.domain.weather.external.kma.KmaResponseDto.WeatherItem;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
        "yyyyMMddHHmm");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public Weather build(Profile profile, List<WeatherItem> weatherItems, BigDecimal latitude,
        BigDecimal longitude) {
        log.debug("[weather builder] 진입, weatherItems.size(): {}", weatherItems.size());

        if (weatherItems == null || weatherItems.isEmpty()) {
            throw new RuntimeException("데이터 없음");
        }

        // 공통값 추출
        WeatherItem any = weatherItems.get(0);
        Instant forecastedAt = toInstantWithZone(any.baseDate(), any.baseTime());
        Instant forecastAt = toInstantWithZone(any.fcstDate(), any.fcstTime());
        Integer xCoord = any.nx();
        Integer yCoord = any.ny();

        // 기본 값 셋팅
        String locationNames = "서울시 중구";
        SkyStatus skyStatus = SkyStatus.CLEAR;
        PrecipitationType precipitationType = PrecipitationType.NONE;
        double precipitationAmount = 0d;
        double precipitationProbability = 0d;
        double humidity = 0d;
        double humidityCompared = 0d;
        double temperature = 0d;
        double temperatureCompared = 0d;
        double temperatureMin = 0d;
        double temperatureMax = 0d;
        double windspeed = 0d;
        WindspeedLevel windspeedLevel = WindspeedLevel.WEAK;

        //카테고리별 데이터 추출
        for (WeatherItem item : weatherItems) {
            String value = item.fcstValue();
            log.debug("item: {}", item.toString());
            KmaCategoryType categoryType = KmaCategoryType.of(item.category());
            switch (categoryType) {
                case SKY -> skyStatus = toSkyStatus(value);
                case PTY -> {
                    System.out.println("precipitationType value = " + value);
                    precipitationType = toPrecipitationType(value);
                    System.out.println("precipitationType after wrap = " + precipitationType);

                }
                case POP -> precipitationProbability = parseDouble(value);
                case PCP -> precipitationAmount = parseDouble(value);
                case REH -> humidity = parseDouble(value);
                case TMP -> temperature = parseDouble(value);
                case TMN -> temperatureMin = parseDouble(value);
                case TMX -> temperatureMax = parseDouble(value);
                case WSD -> {
                    windspeed = parseDouble(value);
                    windspeedLevel = toWindSpeedLevel(windspeed);
                }
                default -> {
                    log.info("tracing 되지않는 카테고리 들어옴 : {}", item.category());
                }
            }
        }

        // weather entity 생성
        return Weather.builder()
            .profile(profile)
            .forecastedAt(forecastedAt)
            .forecastAt(forecastAt)
            .skyStatus(skyStatus)
            .latitude(latitude)
            .longitude(longitude)
            .xCoord(xCoord)
            .yCoord(yCoord)
            .locationNames(locationNames)
            .precipitationType(precipitationType)
            .precipitationAmount(precipitationAmount)
            .precipitationProbability(precipitationProbability)
            .humidity(humidity)
            .humidityCompared(humidityCompared)
            .temperature(temperature)
            .temperatureCompared(temperatureCompared)
            .temperatureMin(temperatureMin)
            .temperatureMax(temperatureMax)
            .windspeed(windspeed)
            .windspeedLevel(windspeedLevel)
            .build();
    }


    private WindspeedLevel toWindSpeedLevel(Double windspeed) {
        if (windspeed < 4) {
            return WindspeedLevel.WEAK;
        } else if (windspeed < 9) {
            return WindspeedLevel.MODERATE;
        } else {
            return WindspeedLevel.STRONG;
        }
    }

    private SkyStatus toSkyStatus(String fcstValue) {
        int skyInt = Integer.parseInt(fcstValue);
        if (skyInt < 6) {
            return SkyStatus.CLEAR;
        } else if (skyInt < 9) {
            return SkyStatus.MOSTLY_CLOUDY;
        } else {
            return SkyStatus.CLOUDY;
        }

    }

    private PrecipitationType toPrecipitationType(String fcstValue) {
        int precipitationInt = Integer.parseInt(fcstValue);
        switch (precipitationInt) {
            case 1:
                return PrecipitationType.RAIN;
            case 2:
                return PrecipitationType.RAIN_SNOW;
            case 3:
                return PrecipitationType.SNOW;
            case 4:
                return PrecipitationType.SHOWER;
            case 0:
            default:
                return PrecipitationType.NONE;
        }
    }

    private Instant toInstantWithZone(String date, String time) {
        LocalDateTime ldt = LocalDateTime.parse(date + time, DATE_TIME_FORMATTER);
        return ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    private double parseDouble(String v) {
        if (v == null || v.isBlank() || "-".equals(v) || "강수없음".equals(v)) {
            return 0d;
        }
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

}
