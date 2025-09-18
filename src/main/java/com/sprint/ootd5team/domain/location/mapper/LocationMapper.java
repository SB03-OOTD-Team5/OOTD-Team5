package com.sprint.ootd5team.domain.location.mapper;

import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Named("toLocationDto")
    default WeatherAPILocationDto toLocationDto(Weather w, @Context ClientCoords ctx) {
        if (w == null) {
            return null;
        }
        String names = w.getLocationNames();
        String[] locationNames =
            (names == null || names.isBlank()) ? new String[0] : names.split(" ");

        return new WeatherAPILocationDto(
            ctx.clientLatitude(), // 클라이언트 위도
            ctx.clientLongitude(), //  클라이언트 경도
            w.getXCoord(),
            w.getYCoord(),
            locationNames,
            w.getLatitude(),
            w.getLongitude()
        );
    }

    default WeatherAPILocationDto toDto(Location location, @Context ClientCoords ctx) {
        if (location == null) {
            return null;
        }
        String names = location.getLocationNames();
        String[] locationNames =
            (names == null || names.isBlank()) ? new String[0] : names.split(" ");
        return new WeatherAPILocationDto(
            ctx.clientLatitude(), // 클라이언트 위도
            ctx.clientLongitude(), //  클라이언트 경도
            location.getXCoord(),
            location.getYCoord(),
            locationNames,
            location.getLatitude(),
            location.getLongitude()
        );
    }
}