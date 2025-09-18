package com.sprint.ootd5team.domain.location.mapper;

import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LocationMapper {

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

    default WeatherAPILocationDto toDto(Location location) {
        return new WeatherAPILocationDto(
            location.getLatitude(),
            location.getLongitude(),
            location.getXCoord(),
            location.getYCoord(),
            location.getLocationNames().split(" ")
        );
    }
}