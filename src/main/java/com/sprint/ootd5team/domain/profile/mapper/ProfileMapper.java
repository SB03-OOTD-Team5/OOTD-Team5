package com.sprint.ootd5team.domain.profile.mapper;

import com.sprint.ootd5team.domain.location.mapper.LocationMapper;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
    LocationMapper.class
})
public abstract class ProfileMapper {

    @Mapping(target = "location", source = ".", qualifiedByName = "profileToLocationDto")
    public abstract ProfileDto toDto(Profile profile);
}
