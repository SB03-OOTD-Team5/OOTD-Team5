package com.sprint.ootd5team.domain.weather.mapper;

import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.mapper.LocationMapper;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {
        TemperatureMapper.class,
        HumidityMapper.class,
        PrecipitationMapper.class,
        WindSpeedMapper.class,
        LocationMapper.class
    }
)
public interface WeatherMapper {

    // ========= Entity -> DTO =========
    @Mapping(target = "id", source = "id")
    @Mapping(target = "precipitation", source = ".", qualifiedByName = "toPrecipitationDto")
    @Mapping(target = "humidity", source = ".", qualifiedByName = "toHumidityDto")
    @Mapping(target = "temperature", source = ".", qualifiedByName = "toTemperatureDto")
    @Mapping(target = "windSpeed", source = ".", qualifiedByName = "toWindSpeedDto")
    WeatherDto toDto(Weather weather, @Context ClientCoords clientCoords);

}
