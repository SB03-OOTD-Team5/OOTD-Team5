package com.sprint.ootd5team.domain.weather.mapper;


import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TemperatureDtoMapper {

    @Named("toTemperatureDto")
    default TemperatureDto toTemperatureDto(Weather w) {
        if (w == null) {
            return null;
        }
        return new TemperatureDto(
            w.getTemperature(),
            w.getTemperatureCompared(),
            w.getTemperatureMin(),
            w.getTemperatureMax()
        );
    }

}