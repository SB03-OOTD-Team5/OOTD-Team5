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
        if (location == null) {
            return null;
        }
        String names = location.getLocationNames();
        String[] locationNames =
            (names == null || names.isBlank()) ? new String[0] : names.split(" ");
        return new WeatherAPILocationDto(
            location.getLatitude(),
            location.getLongitude(),
            location.getXCoord(),
            location.getYCoord(),
            locationNames
        );
    }
}