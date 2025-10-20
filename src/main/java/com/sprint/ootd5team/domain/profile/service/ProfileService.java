package com.sprint.ootd5team.domain.profile.service;

import com.sprint.ootd5team.domain.profile.dto.data.ProfileUpdateRequest;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileDto getProfile(UUID userId);
    ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request, Optional<MultipartFile> profileImage);

}
