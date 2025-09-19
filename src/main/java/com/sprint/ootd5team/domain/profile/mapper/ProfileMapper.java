package com.sprint.ootd5team.domain.profile.mapper;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class ProfileMapper {


    @Mapping(target = "location", expression = "java(getLocation(profile))")
    public abstract ProfileDto toDto(Profile profile);

    protected Location getLocation(Profile profile){
        return new Location(profile.getLatitude(),profile.getLongitude(),profile.getXCoord(),
            profile.getYCoord(), profile.getLocationNames());
    }

}
