package com.sprint.ootd5team.domain.profile.mapper;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.location.mapper.LocationMapper;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {
    LocationMapper.class
})
public abstract class ProfileMapper {

    @Autowired
    private FileStorage fileStorage;

    @Mapping(target = "location", source = ".", qualifiedByName = "profileToLocationDto")
    @Mapping(target = "userId",source = "user.id")
    @Mapping(source = "profileImageUrl", target = "profileImageUrl", qualifiedByName = "resolveImageUrl")
    public abstract ProfileDto toDto(Profile profile);

    @Mapping(target = "userId",source = "user.id")
    public abstract AuthorDto toAuthorDto(Profile profile);

    @Named("resolveImageUrl")
    protected String resolveImageUrl(String path) {
        return fileStorage.resolveUrl(path);
    }
}
