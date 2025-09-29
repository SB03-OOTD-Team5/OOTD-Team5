package com.sprint.ootd5team.domain.location.mapper;

import com.sprint.ootd5team.domain.location.dto.data.ClientCoords;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LocationMapper {

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

    /* profile 내부에서 locationDto로 변경할때 */
    @Named("profileToLocationDto")
    default WeatherAPILocationDto profileToLocationDto(Profile profile) {
        if (profile == null) {
            return null;
        }

        if(profile.getLocation()==null){
            return null;
        }

        String names = profile.getLocation().getLocationNames();
        String[] locationNames =
            (names == null || names.isBlank()) ? new String[0] : names.split(" ");

        return new WeatherAPILocationDto(
            profile.getLocation().getLatitude(),
            profile.getLocation().getLongitude(),
            profile.getLocation().getXCoord(),
            profile.getLocation().getYCoord(),
            locationNames,
            profile.getLocation().getLatitude(),
            profile.getLocation().getLongitude()
        );
    }

}