package com.sprint.ootd5team.domain.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.location.repository.LocationRepository;
import com.sprint.ootd5team.domain.profile.dto.data.ProfileUpdateRequest;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileService 단위 테스트")
@ActiveProfiles("test")
public class ProfileServiceTest {

    @Mock
    ProfileRepository profileRepository;

    @Mock
    ProfileMapper profileMapper;

    @Mock
    FileStorage fileStorage;

    @Mock
    LocationRepository locationRepository;

    @InjectMocks
    ProfileServiceImpl profileService;

    private UUID testUserId;
    private UUID testProfileId;
    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testProfileId = UUID.randomUUID();

        testUser = new User("testuser", "test@test.com", "password", Role.USER);
        ReflectionTestUtils.setField(testUser, "id", testUserId);

        testProfile = new Profile(testUser, "테스트닉네임", null, null, null, null, null);
        ReflectionTestUtils.setField(testProfile, "id", testProfileId);
    }

    @Test
    @DisplayName("존재하는 userId로 프로필 조회 시 ProfileDto 반환")
    void getProfile_returnsProfileDto_whenUserExists() {
        // given
        ProfileDto expectedDto = new ProfileDto(
            testUserId,
            "테스트닉네임",
            null,
            null,
            null,
            0,
            null
        );

        given(profileRepository.findByUserId(testUserId)).willReturn(Optional.of(testProfile));
        given(profileMapper.toDto(testProfile)).willReturn(expectedDto);

        // when
        ProfileDto result = profileService.getProfile(testUserId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(testUserId);
        assertThat(result.name()).isEqualTo("테스트닉네임");

        verify(profileRepository).findByUserId(testUserId);
        verify(profileMapper).toDto(testProfile);
    }

    @Test
    @DisplayName("존재하지 않는 userId로 프로필 조회 시 ProfileNotFoundException 반환")
    void getProfile_throwsException_whenUserNotFound() {
        // given
        UUID nonExistentUserId = UUID.randomUUID();
        given(profileRepository.findByUserId(nonExistentUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.getProfile(nonExistentUserId))
            .isInstanceOf(ProfileNotFoundException.class);

        verify(profileRepository).findByUserId(nonExistentUserId);
    }

    @Test
    @DisplayName("이미지/위치 없이 프로필 업데이트 후 dto 반환")
    void updateProfile_updatesWithoutImageAndLocation() {
        // given
        var request = mock(ProfileUpdateRequest.class);
        given(request.location()).willReturn(null);
        given(request.name()).willReturn("변경이름");
        given(request.gender()).willReturn(null);
        given(request.birthDate()).willReturn(null);
        given(request.temperatureSensitivity()).willReturn(1);

        given(profileRepository.findByUserId(testUserId)).willReturn(Optional.of(testProfile));
        given(profileRepository.save(testProfile)).willReturn(testProfile);

        ProfileDto expectedDto = new ProfileDto(
            testUserId, "변경이름", null, null, null, 1, null
        );
        given(profileMapper.toDto(testProfile)).willReturn(expectedDto);

        // when
        ProfileDto result = profileService.updateProfile(testUserId, request, Optional.empty());

        // then
        assertThat(result).isSameAs(expectedDto);
        verify(profileRepository).findByUserId(testUserId);
        verify(profileRepository).save(testProfile);
        verify(profileMapper).toDto(testProfile);
    }

    @Test
    @DisplayName("이미지 업로드 성공 시, url 업데이트 후 이전 파일 삭제")
    void updateProfile_withImage_success_deletesPrevious() throws Exception {
        // given
        ReflectionTestUtils.setField(testProfile, "profileImageUrl", "prev-url");

        var request = mock(ProfileUpdateRequest.class);
        given(request.location()).willReturn(null);
        given(request.name()).willReturn("변경이름");
        given(request.gender()).willReturn(null);
        given(request.birthDate()).willReturn(null);
        given(request.temperatureSensitivity()).willReturn(0);

        var file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("a.png");
        given(file.getContentType()).willReturn("image/png");
        given(file.getInputStream()).willReturn(new java.io.ByteArrayInputStream("img".getBytes()));

        given(profileRepository.findByUserId(testUserId)).willReturn(Optional.of(testProfile));
        given(fileStorage.upload(eq("a.png"), any(java.io.InputStream.class), eq("image/png"), any()))
            .willReturn("new-url");

        given(profileRepository.save(testProfile)).willReturn(testProfile);

        ProfileDto expectedDto = new ProfileDto(
            testUserId,
            "변경이름",
            null,
            null,
            null,
            0,
            "new-url"
        );
        given(profileMapper.toDto(testProfile)).willReturn(expectedDto);

        // when
        ProfileDto result = profileService.updateProfile(testUserId, request, Optional.of(file));

        // then
        assertThat(result).isSameAs(expectedDto);
        verify(fileStorage).upload(eq("a.png"), any(java.io.InputStream.class), eq("image/png"), any());
        verify(fileStorage).delete("prev-url");
        verify(profileRepository).save(testProfile);
    }
}