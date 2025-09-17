package com.sprint.ootd5team.domain.weather.mapper;

import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PrecipitationMapper {

    @Named("toPrecipitationDto")
    default PrecipitationDto toPrecipitationDto(Weather w) {
        if (w == null) {
            return null;
        }
        return new PrecipitationDto(
            w.getPrecipitationType(),
            w.getPrecipitationAmount(),
            w.getPrecipitationProbability()
        );
    }
}
