package com.sprint.ootd5team.domain.recommendation.mapper;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.recommendation.dto.ProfileInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecommendationInfoMapper {

    @Mapping(target = "weatherInfo", source = "weather")
    @Mapping(target = "profileInfo", source = "profile")
    RecommendationInfoDto toDto(Weather weather, Profile profile);

    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "age", expression = "java(calculateAge(profile.getBirthDate()))")
    @Mapping(target = "temperatureSensitivity", source = "temperatureSensitivity")
    ProfileInfoDto toProfileInfoDto(Profile profile);

    @Mapping(target = "precipitationType", source = "precipitationType")
    @Mapping(target = "precipitationAmount", source = "precipitationAmount")
    @Mapping(target = "currentHumidity", source = "humidity")
    @Mapping(target = "currentTemperature", source = "temperature")
    @Mapping(target = "minTemperature", source = "temperatureMin")
    @Mapping(target = "maxTemperature", source = "temperatureMax")
    @Mapping(target = "windSpeedLevel", source = "windspeedLevel")
    WeatherInfoDto toWeatherInfoDto(Weather weather);

    default int calculateAge(LocalDate birthDate) {
        return (birthDate == null) ? 0
            : java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }

    default int defaultSensitivity(Integer sensitivity) {
        return (sensitivity == null) ? 2 : sensitivity;
    }
}