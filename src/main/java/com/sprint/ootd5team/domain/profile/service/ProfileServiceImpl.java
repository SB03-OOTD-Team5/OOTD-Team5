package com.sprint.ootd5team.domain.profile.service;

import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.profile.dto.data.ProfileUpdateRequest;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final FileStorage fileStorage;

    @Value("${ootd.storage.s3.prefix.profiles}")
    private String profilesPrefix;


    /**
     * 프로필 정보를 가져오는 메서드
     * @param userId 요청한 userId
     * @return 해당 유저의 프로필 Dto
     */
    @Override
    public ProfileDto getProfile(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(UserNotFoundException::new);

        return profileMapper.toDto(profile);
    }

    /**
     * 프로필 정보를 업데이트 하는 메서드
     * @param userId 업데이트 요청한 userId
     * @param request 업데이트 정보(name, gender, birthDate, location, temperatureSensitivity)
     * @param profileImage (선택) 설정할 프로필 이미지
     * @return 변경된 프로필 Dto
     */
    @Override
    public ProfileDto updateProfile(UUID userId, ProfileUpdateRequest request,
        Optional<MultipartFile> profileImage) {

        // 해당 userId의 프로필이 존재하는지 확인
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(ProfileNotFoundException::new);

        profileImage.ifPresent(image -> {
            String previousImageUrl = profile.getProfileImageUrl();

            try (InputStream in = image.getInputStream()) {
                String profileImageUrl = fileStorage.upload(
                    image.getOriginalFilename(), in, image.getContentType(), profilesPrefix
                );
                profile.updateProfileImageUrl(profileImageUrl);
                log.debug("[Profile] 이미지 업로드 완료: url={}", profileImageUrl);

                if (previousImageUrl != null) {
                    fileStorage.delete(previousImageUrl);
                }
            } catch (IOException e) {
                log.warn("[Profile] 이미지 업로드 실패 {}", e.getMessage());
                throw FileSaveFailedException.withFileName(image.getOriginalFilename());
            }
        });


        // 프로필 업데이트
        profile.update(request.name(), request.gender(), request.birthDate(),request.location(),
            request.temperatureSensitivity());

        return profileMapper.toDto(profileRepository.save(profile));

    }
}
