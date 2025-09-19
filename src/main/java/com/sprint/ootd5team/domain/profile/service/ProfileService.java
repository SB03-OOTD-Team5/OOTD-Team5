package com.sprint.ootd5team.domain.profile.service;

import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import java.util.UUID;

public interface ProfileService {

    ProfileDto getProfile(UUID userId);

}
