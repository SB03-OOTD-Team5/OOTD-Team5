package com.sprint.ootd5team.domain.weather.mapper;

import com.sprint.ootd5team.domain.weather.dto.data.WindSpeedDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface WindSpeedDtoMapper {

    @Named("toWindSpeedDto")
    default WindSpeedDto toWindSpeedDto(Weather w) {
        if (w == null) {
            return null;
        }
        return new WindSpeedDto(
            w.getWindspeed(),
            w.getWindspeedLevel()
        );
    }
}