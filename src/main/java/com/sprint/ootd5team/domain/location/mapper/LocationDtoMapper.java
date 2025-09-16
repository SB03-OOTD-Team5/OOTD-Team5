package com.sprint.ootd5team.domain.location.mapper;

import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LocationDtoMapper {

    @Named("toLocationDto")
    default WeatherAPILocationDto toLocationDto(Weather w) {
        if (w == null) {
            return null;
        }
        return new WeatherAPILocationDto(
            w.getLatitude(),
            w.getLongitude(),
            w.getXCoord(),
            w.getYCoord(),
            w.getLocationNames().split(" ")
        );
    }
}