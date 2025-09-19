package com.sprint.ootd5team.domain.profile.service;

import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;


    @Override
    public ProfileDto getProfile(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(UserNotFoundException::new);

        return profileMapper.toDto(profile);
    }
}
