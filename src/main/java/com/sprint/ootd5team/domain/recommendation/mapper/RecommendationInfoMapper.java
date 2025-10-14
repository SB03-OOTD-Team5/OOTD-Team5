package com.sprint.ootd5team.domain.recommendation.mapper;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.recommendation.dto.ProfileInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationInfoDto;
import com.sprint.ootd5team.domain.recommendation.dto.WeatherInfoDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import java.time.LocalDate;
import java.time.Period;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RecommendationInfoMapper {

    @Mapping(target = "personalFeelsTemp",
        expression = "java(weatherInfo.apparentTemperature().calculatePersonalFeelsLike(profileInfo.temperatureSensitivity()))")
    RecommendationInfoDto toDto(WeatherInfoDto weatherInfo, ProfileInfoDto profileInfo);

    @Mapping(target = "age", source = "birthDate", qualifiedByName = "calculateAge")
    @Mapping(target = "temperatureSensitivity", source = "temperatureSensitivity", qualifiedByName = "defaultSensitivity")
    ProfileInfoDto toProfileInfoDto(Profile profile);

    @Mapping(target = "apparentTemperature", source = "weather")
    WeatherInfoDto toWeatherInfoDto(Weather weather);

    @Named("calculateAge")
    default int calculateAge(LocalDate birthDate) {
        return (birthDate == null) ? 0 : Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Named("defaultSensitivity")
    default int defaultSensitivity(Integer sensitivity) {
        return (sensitivity == null) ? 3 : sensitivity;
    }
}