package com.sprint.ootd5team.domain.user.mapper;

import com.sprint.ootd5team.domain.oauthuser.entity.OauthUser;
import com.sprint.ootd5team.domain.oauthuser.repository.OauthRepository;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.entity.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected OauthRepository oauthRepository;

    @Mapping(target = "linkedOAuthProviders",expression = "java(getLinkedOAuthProviders(user))")
    public abstract UserDto toDto(User user);

    protected List<String> getLinkedOAuthProviders(User user){
        return oauthRepository.findByUserId(user.getId())
            .stream()
            .map(OauthUser::getProvider)
            .toList();
    }
}
