package com.sprint.ootd5team.recommendation.fixture;

import com.sprint.ootd5team.domain.recommendation.dto.ApparentTemperatureDto;
import com.sprint.ootd5team.domain.recommendation.dto.ProfileInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import com.sprint.ootd5team.domain.weather.enums.WindspeedLevel;
import java.time.Instant;

public final class RecommendationFixture {

    private RecommendationFixture() {
    }

    /** 기본 추천 정보 (맑음, 무강수, 약한 바람, 남성 25세, 보통 민감도) */
    public static RecommendationInfoDto defaultInfo() {
        return create(
            PrecipitationType.NONE,
            0.0,
            WindspeedLevel.WEAK,
            SkyStatus.CLEAR,
            20.0,
            50.0,           // humidity
            1.2,            // windspeed (m/s)
            Instant.now(),
            25,
            "MALE",
            3
        );
    }

    /** 비 오는 날 테스트용 */
    public static RecommendationInfoDto rainyInfo() {
        return create(
            PrecipitationType.RAIN,
            80.0,
            WindspeedLevel.MODERATE,
            SkyStatus.CLOUDY,
            18.0,
            85.0,
            3.2,
            Instant.now(),
            25,
            "FEMALE",
            1
        );
    }

    /** 추운 날씨 테스트용 */
    public static RecommendationInfoDto coldInfo() {
        return create(
            PrecipitationType.SNOW,
            60.0,
            WindspeedLevel.STRONG,
            SkyStatus.CLOUDY,
            -3.0,
            40.0,
            5.0,
            Instant.now(),
            30,
            "MALE",
            2
        );
    }

    /** 더운 날씨 테스트용 */
    public static RecommendationInfoDto hotInfo() {
        return create(
            PrecipitationType.NONE,
            0.0,
            WindspeedLevel.WEAK,
            SkyStatus.CLEAR,
            33.0,
            70.0,
            0.8,
            Instant.now(),
            22,
            "FEMALE",
            5
        );
    }

    /**
     * 커스텀 추천정보 생성
     */
    public static RecommendationInfoDto create(
        PrecipitationType precipitationType,
        double precipitationProbability,
        WindspeedLevel windspeedLevel,
        SkyStatus skyStatus,
        double temperature,
        double humidity,
        double windspeed,
        Instant forecastAt,
        int age,
        String gender,
        int tempSensitivity
    ) {
        ApparentTemperatureDto apparent = new ApparentTemperatureDto(
            temperature,
            humidity,
            windspeed,
            forecastAt
        );

        WeatherInfoDto weatherInfo = new WeatherInfoDto(
            precipitationType,
            precipitationProbability,
            windspeedLevel,
            skyStatus,
            apparent
        );

        ProfileInfoDto profileInfo = new ProfileInfoDto(
            gender,
            age,
            tempSensitivity
        );

        double feels = apparent.calculatePersonalFeelsLike(tempSensitivity);
        return new RecommendationInfoDto(weatherInfo, profileInfo, feels);
    }
}