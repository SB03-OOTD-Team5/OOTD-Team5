package com.sprint.ootd5team.domain.weather.mapper;


import com.sprint.ootd5team.domain.weather.dto.data.HumidityDto;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface HumidityMapper {

    @Named("toHumidityDto")
    default HumidityDto toHumidityDto(Weather w) {
        if (w == null) {
            return null;
        }
        return new HumidityDto(
            w.getHumidity(),
            w.getHumidityCompared()
        );
    }
}
