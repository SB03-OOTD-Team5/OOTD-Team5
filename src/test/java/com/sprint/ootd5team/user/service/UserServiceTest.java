package com.sprint.ootd5team.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.base.exception.user.UserAlreadyExistException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.location.dto.data.WeatherAPILocationDto;
import com.sprint.ootd5team.domain.profile.dto.data.ProfileUpdateRequest;
import com.sprint.ootd5team.domain.profile.dto.request.ProfileDto;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.mapper.ProfileMapper;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.profile.service.ProfileService;
import com.sprint.ootd5team.domain.profile.service.ProfileServiceImpl;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ChangePasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserCreateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserLockUpdateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import com.sprint.ootd5team.domain.user.dto.response.UserDtoCursorResponse;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.domain.user.repository.UserRepositoryCustom;
import com.sprint.ootd5team.domain.user.service.UserService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(classes = {AuthService.class, UserService.class, ProfileServiceImpl.class})
@EnableMethodSecurity
class UserServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileService profileService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private JwtRegistry jwtRegistry;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepositoryCustom userRepositoryCustom;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private ProfileMapper profileMapper;

    @MockitoBean
    private FileStorage fileStorage;


    private User testUser;
    private UserDto testUserDto;
    private UserCreateRequest userCreateRequest;
    private ChangePasswordRequest changePasswordRequest;


    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();
        testUser = new User("testName", "test@email.com", "encodedPassword", Role.USER);
        ReflectionTestUtils.setField(testUser, "id", userId);

        testUserDto = new UserDto(
            userId,
            Instant.now(),
            "test@email.com",
            "testName",
            Role.USER,
            null,
            false
        );

        userCreateRequest = new UserCreateRequest(
            "testName",
            "test@email.com",
            "password123"
        );

        changePasswordRequest = new ChangePasswordRequest("newPassword123");
    }

    @Test
    @DisplayName("유저 생성 성공")
    void create_Success() {
        // given
        given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(false);
        given(passwordEncoder.encode(userCreateRequest.password())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(userMapper.toDto(testUser)).willReturn(testUserDto);
        given(profileRepository.save(any(Profile.class))).willReturn(any(Profile.class));

        // when
        UserDto result = userService.create(userCreateRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(userCreateRequest.email());
        assertThat(result.name()).isEqualTo(userCreateRequest.name());

        verify(userRepository).existsByEmail(userCreateRequest.email());
        verify(passwordEncoder).encode(userCreateRequest.password());
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(Profile.class));
        verify(userMapper).toDto(testUser);
    }

    @Test
    @DisplayName("유저 생성 실패 - 이메일 중복")
    void create_Fail_EmailAlreadyExists() {
        // given
        given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(userCreateRequest))
            .isInstanceOf(UserAlreadyExistException.class);

        verify(userRepository).existsByEmail(userCreateRequest.email());
        verify(userRepository, never()).save(any(User.class));
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // given
        UUID userId = testUser.getId();
        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(passwordEncoder.encode(changePasswordRequest.password())).willReturn("newEncodedPassword");
        given(userRepository.save(testUser)).willReturn(testUser);

        // when
        userService.changePassword(userId, changePasswordRequest);

        // then
        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(changePasswordRequest.password());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 사용자 없음")
    void changePassword_Fail_UserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.changePassword(userId, changePasswordRequest))
            .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("커서 기반 유저 조회 성공 - 다음 페이지 있음")
    void getUsers_Success_HasNext() {
        // given
        String cursor = "2024-01-01T00:00:00";
        UUID idAfter = UUID.randomUUID();
        Integer limit = 2;
        String sortBy = "createdAt";
        String sortDirection = "DESC";
        String emailLike = null;
        String roleEqual = null;
        Boolean locked = null;

        User user1 = createTestUser("user1@test.com", "User1");
        User user2 = createTestUser("user2@test.com", "User2");
        User user3 = createTestUser("user3@test.com", "User3"); // limit+1 번째

        List<User> users = Arrays.asList(user1, user2, user3);

        UserDto userDto1 = createTestUserDto(user1);
        UserDto userDto2 = createTestUserDto(user2);
        UserDto userDto3 = createTestUserDto(user3);

        given(userRepositoryCustom.findUsersWithCursor(
            cursor, idAfter, limit + 1, sortBy, sortDirection, emailLike, roleEqual, locked))
            .willReturn(users);

        given(userMapper.toDto(user1)).willReturn(userDto1);
        given(userMapper.toDto(user2)).willReturn(userDto2);
        given(userMapper.toDto(user3)).willReturn(userDto3);

        given(userRepositoryCustom.countUsers(roleEqual, emailLike, locked)).willReturn(10L);

        // when
        UserDtoCursorResponse result = userService.getUsers(
            cursor, idAfter, limit, sortBy, sortDirection, emailLike, roleEqual, locked);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2); // limit+1에서 마지막 하나 제거됨
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(userDto3.createdAt().toString());
        assertThat(result.nextIdAfter()).isEqualTo(userDto3.id());
        assertThat(result.totalCount()).isEqualTo(10L);
        assertThat(result.sortBy()).isEqualTo(sortBy);
        assertThat(result.sortDirection()).isEqualTo(sortDirection);
    }

    @Test
    @DisplayName("커서 기반 유저 조회 성공 - 다음 페이지 없음")
    void getUsers_Success_NoNext() {
        // given
        String cursor = "2024-01-01T00:00:00";
        UUID idAfter = UUID.randomUUID();
        Integer limit = 3;
        String sortBy = "email";
        String sortDirection = "ASC";
        String emailLike = "test";
        String roleEqual = "USER";
        Boolean locked = false;

        User user1 = createTestUser("user1@test.com", "User1");
        User user2 = createTestUser("user2@test.com", "User2");

        List<User> users = Arrays.asList(user1, user2); // limit 보다 적음

        UserDto userDto1 = createTestUserDto(user1);
        UserDto userDto2 = createTestUserDto(user2);

        given(userRepositoryCustom.findUsersWithCursor(
            cursor, idAfter, limit + 1, sortBy, sortDirection, emailLike, roleEqual, locked))
            .willReturn(users);

        given(userMapper.toDto(user1)).willReturn(userDto1);
        given(userMapper.toDto(user2)).willReturn(userDto2);

        given(userRepositoryCustom.countUsers(roleEqual, emailLike, locked)).willReturn(2L);

        // when
        UserDtoCursorResponse result = userService.getUsers(
            cursor, idAfter, limit, sortBy, sortDirection, emailLike, roleEqual, locked);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.nextIdAfter()).isNull();
        assertThat(result.totalCount()).isEqualTo(2L);
        assertThat(result.sortBy()).isEqualTo(sortBy);
        assertThat(result.sortDirection()).isEqualTo(sortDirection);
    }

    @Test
    @DisplayName("커서 기반 유저 조회 - email 정렬 시 다음 커서 설정")
    void getUsers_EmailSort_NextCursor() {
        // given
        String cursor = "a@test.com";
        UUID idAfter = UUID.randomUUID();
        Integer limit = 1;
        String sortBy = "email";
        String sortDirection = "ASC";

        User user1 = createTestUser("b@test.com", "User1");
        User user2 = createTestUser("c@test.com", "User2"); // limit+1 번째

        List<User> users = Arrays.asList(user1, user2);

        UserDto userDto1 = createTestUserDto(user1);
        UserDto userDto2 = createTestUserDto(user2);

        given(userRepositoryCustom.findUsersWithCursor(
            cursor, idAfter, limit + 1, sortBy, sortDirection, null, null, null))
            .willReturn(users);

        given(userMapper.toDto(user1)).willReturn(userDto1);
        given(userMapper.toDto(user2)).willReturn(userDto2);

        given(userRepositoryCustom.countUsers(null, null, null)).willReturn(5L);

        // when
        UserDtoCursorResponse result = userService.getUsers(
            cursor, idAfter, limit, sortBy, sortDirection, null, null, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(userDto2.email()); // email 정렬시 email이 커서
        assertThat(result.nextIdAfter()).isEqualTo(userDto2.id());
    }

    @Test
    @DisplayName("사용자 Role 수정 성공")
    @WithMockUser(username = "tester", roles = {"ADMIN"})
    void update_UserRole_Success() {

        UserDto testUserDto = new UserDto(
            testUser.getId(),
            testUser.getCreatedAt(),
            testUser.getEmail(),
            testUser.getName(),
            Role.ADMIN,
            null,
            false
        );

        given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        given(userRepository.save(testUser)).willReturn(testUser);
        given(userMapper.toDto(testUser)).willReturn(testUserDto);

        // when
        UserDto result = authService.updateRoleInternal(testUser.getId(),new UserRoleUpdateRequest("ADMIN"));

        // then
        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("사용자 Role 수정 실패 - 존재하지 않는 사용자")
    @WithMockUser(username = "tester", roles = {"ADMIN"})
    void update_UserRole_Fail_UserNotFound() {

        UserDto testUserDto = new UserDto(
            testUser.getId(),
            testUser.getCreatedAt(),
            testUser.getEmail(),
            testUser.getName(),
            Role.ADMIN,
            null,
            false
        );

        given(userRepository.findById(testUser.getId())).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> authService.updateRoleInternal(testUser.getId(), new UserRoleUpdateRequest("ADMIN")))
            .isInstanceOf(UserNotFoundException.class);

        // then
        verify(userRepository).findById(testUser.getId());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("사용자 Role 수정 실패 - 권한이 없는 사용자")
    @WithMockUser( roles = {"USER"})
    void update_UserRole_Fail_UnAuthorized() {

        assertThatThrownBy(
            () -> authService.updateRoleInternal(testUser.getId(), new UserRoleUpdateRequest("ADMIN")))
            .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    @DisplayName("사용자 프로필 수정 성공")
    void update_UserProfile_Success() {
        BigDecimal latitude = BigDecimal.valueOf(37.52);
        BigDecimal longitude = BigDecimal.valueOf(129.11);
        String[] locationNames = {"서울특별시", "광진구", "능동", ""};

        WeatherAPILocationDto weatherAPILocationDto = new WeatherAPILocationDto(
            latitude, longitude, 96, 127, locationNames, latitude, longitude);

        ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest(
            "updatedName", "MALE", LocalDate.now(), weatherAPILocationDto, 4);

        ProfileDto profileDto = new ProfileDto(
            testUser.getId(), "updatedName", "MALE", LocalDate.now(), weatherAPILocationDto, 4, null);

        Profile profile = new Profile(testUser,testUser.getName(),null,null,null,null,null);

        // given
        given(profileRepository.findByUserId(testUser.getId())).willReturn(Optional.of(profile));
        given(profileRepository.save(any(Profile.class))).willReturn(profile);
        given(profileMapper.toDto(any(Profile.class))).willReturn(profileDto);

        // when
        ProfileDto result = profileService.updateProfile(testUser.getId(),profileUpdateRequest,Optional.ofNullable(null));

        // then
        assertThat(result).isNotNull();
        assertThat(result.birthDate()).isNotNull();
        assertThat(result.location()).isEqualTo(weatherAPILocationDto);
        assertThat(result.name()).isEqualTo("updatedName");
        assertThat(result.gender()).isEqualTo("MALE");
        assertThat(result.temperatureSensitivity()).isEqualTo(4);
    }

    @Test
    @DisplayName("사용자 프로필 수정 실패 - 존재하지 않는 사용자")
    void update_UserProfile_Fail_ProfileNotFound() {
        BigDecimal latitude = BigDecimal.valueOf(37.52);
        BigDecimal longitude = BigDecimal.valueOf(129.11);
        String[] locationNames = {"서울특별시", "광진구", "능동", ""};

        WeatherAPILocationDto weatherAPILocationDto = new WeatherAPILocationDto(
            latitude, longitude, 96, 127, locationNames, latitude, longitude);

        ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest(
            "updatedName", "MALE", LocalDate.now(), weatherAPILocationDto, 4);

        // given
        given(profileRepository.findByUserId(testUser.getId())).willReturn(Optional.empty());

        // when
        assertThatThrownBy(
            () -> profileService.updateProfile(testUser.getId(),profileUpdateRequest,Optional.ofNullable(null)))
            .isInstanceOf(ProfileNotFoundException.class);

        // then
        verify(profileMapper,never()).toDto(any(Profile.class));
        verify(profileRepository,never()).save(any(Profile.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("계정 잠금 상태 변경 성공")
    void updateUserLock_Success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = new User("테스트", "test@example.com", "encodedPassword", Role.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        UserLockUpdateRequest request = new UserLockUpdateRequest(true);
        UserDto userDto = new UserDto(userId, user.getCreatedAt(), user.getEmail(), user.getName(), Role.USER, null, true);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class))).willReturn(user);
        given(userMapper.toDto(any(User.class))).willReturn(userDto);

        // when
        UserDto result = userService.updateUserLock(userId, request);

        // then
        assertThat(result.locked()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("계정 잠금 상태 변경 실패 - 유저 없음")
    void updateUserLock_Fail_UserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UserLockUpdateRequest request = new UserLockUpdateRequest(true);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserLock(userId, request))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "USER") // 일반 사용자 권한
    @DisplayName("계정 잠금 상태 변경 실패 - ADMIN이 아님")
    void updateUserLock_Fail_Forbidden() {

        // when & then
        assertThatThrownBy(
            () -> userService.updateUserLock(testUser.getId(), new UserLockUpdateRequest(true)))
            .isInstanceOf(AccessDeniedException.class);

        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDto(any(User.class));
    }


    private User createTestUser(String email, String name) {
        User user = new User(name, email, "encodedPassword", Role.USER);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private UserDto createTestUserDto(User user) {
        return new UserDto(
            user.getId(),
            Instant.now(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            null,
            false
        );
    }


}