package com.sprint.ootd5team.base.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;
import com.sprint.ootd5team.base.exception.user.UserNotFoundException;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdSecurityUserDetails;
import com.sprint.ootd5team.domain.notification.event.type.single.RoleUpdatedEvent;
import com.sprint.ootd5team.domain.user.dto.TemporaryPasswordCreatedEvent;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.dto.request.ResetPasswordRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.mapper.UserMapper;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @Mock JwtTokenProvider tokenProvider;
    @Mock JwtRegistry jwtRegistry;
    @Mock UserDetailsService userDetailsService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("역할 업데이트 성공")
    void updateRoleInternal_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(Role.USER);

        User saved = mock(User.class);
        when(userRepository.save(user)).thenReturn(saved);

        UserDto dto = mock(UserDto.class);
        when(userMapper.toDto(saved)).thenReturn(dto);

        UserRoleUpdateRequest req = new UserRoleUpdateRequest("ADMIN");
        when(user.getId()).thenReturn(userId);

        // when
        UserDto result = authService.updateRoleInternal(userId, req);

        // then
        assertThat(result).isSameAs(dto);
        verify(user).updateRole(Role.ADMIN);
        verify(jwtRegistry).invalidateJwtInformationByUserId(userId);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(RoleUpdatedEvent.class);
    }

    @Test
    @DisplayName("조회되지 않는 유저이면 UserNotFoundException 반환")
    void updateRoleInternal_userNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.updateRoleInternal(userId, new UserRoleUpdateRequest("ADMIN")))
            .isInstanceOf(UserNotFoundException.class);

        verify(jwtRegistry, never()).invalidateJwtInformationByUserId(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("비밀번호 초기화 및 이벤트 발행 성공")
    void resetPassword_success_publishesEvent() {
        // given
        User user = mock(User.class);
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        when(user.getTempPassword()).thenReturn("temp");
        when(user.getEmail()).thenReturn("test@mail.com");
        when(user.getName()).thenReturn("name");
        when(user.getTempPasswordExpireAt()).thenReturn(java.time.Instant.now());
        when(user.getId()).thenReturn(UUID.randomUUID());

        // when
        authService.resetPassword(new ResetPasswordRequest("test@mail.com"));

        // then
        verify(user).issueTemporaryPassword();
        verify(userRepository).save(user);
        verify(eventPublisher).publishEvent(any(TemporaryPasswordCreatedEvent.class));
    }

    @Test
    @DisplayName("이메일로 조회되지 않는 유저일 시 UserNotFoundException 반환")
    void resetPassword_userNotFound() {
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(new ResetPasswordRequest("test@mail.com")))
            .isInstanceOf(UserNotFoundException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 INVALID_TOKEN 예외 반환")
    void refreshToken_invalidToken_throws() {
        // given
        String refresh = "refresh";
        when(tokenProvider.validateRefreshToken(refresh)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(refresh))
            .isInstanceOf(OotdException.class)
            .satisfies(ex ->
                assertThat(((OotdException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));

        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("registry에 active 없으면 INVALID_TOKEN 예외 반환")
    void refreshToken_notActive_throws() {
        String refresh = "refresh";

        when(tokenProvider.validateRefreshToken(refresh))
            .thenReturn(true);
        when(jwtRegistry.hasActiveJwtInformationByRefreshToken(refresh))
            .thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refresh))
            .isInstanceOf(OotdException.class)
            .satisfies(ex ->
                assertThat(((OotdException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN));
    }

    @Test
    @DisplayName("UserDetails 타입이 다르면 INVALID_USER_DETAILS 예외 반환")
    void refreshToken_invalidUserDetailsType_throws() {
        String refresh = "refresh";

        when(tokenProvider.validateRefreshToken(refresh))
            .thenReturn(true);
        when(jwtRegistry.hasActiveJwtInformationByRefreshToken(refresh)).
            thenReturn(true);
        when(tokenProvider.getEmailFromToken(refresh))
            .thenReturn("user@test.com");

        UserDetails springUser = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("user@test.com"))
            .thenReturn(springUser);

        assertThatThrownBy(() -> authService.refreshToken(refresh))
            .isInstanceOf(OotdException.class)
            .satisfies(ex ->
                assertThat(((OotdException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_USER_DETAILS));
    }

    @Test
    @DisplayName("성공 시 rotateJwtInformation 호출 및 새 JwtInformation 반환")
    void refreshToken_success_rotates() throws Exception {
        String oldRefresh = "oldRefresh";

        when(tokenProvider.validateRefreshToken(oldRefresh))
            .thenReturn(true);
        when(jwtRegistry.hasActiveJwtInformationByRefreshToken(oldRefresh))
            .thenReturn(true);
        when(tokenProvider.getEmailFromToken(oldRefresh))
            .thenReturn("user@test.com");

        OotdSecurityUserDetails userDetails = mock(OotdSecurityUserDetails.class);
        UserDto userDto = mock(UserDto.class);

        when(userDetails.getUserDto())
            .thenReturn(userDto);
        when(userDetails.getUserId())
            .thenReturn(UUID.randomUUID());
        when(userDetailsService.loadUserByUsername("user@test.com"))
            .thenReturn(userDetails);

        when(tokenProvider.generateAccessToken(userDto))
            .thenReturn("newAccess");
        when(tokenProvider.generateRefreshToken(userDto))
            .thenReturn("newRefresh");

        // when
        JwtInformation result = authService.refreshToken(oldRefresh);

        // then
        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        assertThat(result.getRefreshToken()).isEqualTo("newRefresh");
        verify(jwtRegistry).rotateJwtInformation(eq(oldRefresh), any(JwtInformation.class));
    }

    @Test
    @DisplayName("JOSEException이면 INTERNAL_SERVER_ERROR 예외 반환")
    void refreshToken_joseException_throwsInternal() throws Exception {
        String oldRefresh = "oldRefresh";
        when(tokenProvider.validateRefreshToken(oldRefresh)).thenReturn(true);
        when(jwtRegistry.hasActiveJwtInformationByRefreshToken(oldRefresh)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(oldRefresh)).thenReturn("user@test.com");

        OotdSecurityUserDetails userDetails = mock(OotdSecurityUserDetails.class);
        UserDto userDto = mock(UserDto.class);
        when(userDetails.getUserDto()).thenReturn(userDto);
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        when(tokenProvider.generateAccessToken(userDto)).thenThrow(new JOSEException("fail"));

        assertThatThrownBy(() -> authService.refreshToken(oldRefresh))
            .isInstanceOf(OotdException.class)
            .satisfies(ex ->
                assertThat(((OotdException) ex).getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("인증 없음/미인증이면 UNAUTHORIZED 예외 반환")
    void getCurrentUserId_unauthorized() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> authService.getCurrentUserId())
            .isInstanceOf(OotdException.class)
            .satisfies(ex ->
                assertThat(((OotdException) ex).getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
    }

    @Test
    @DisplayName("principal이 OotdSecurityUserDetails면 userId 반환")
    void getCurrentUserId_success() {
        UUID userId = UUID.randomUUID();
        OotdSecurityUserDetails principal = mock(OotdSecurityUserDetails.class);

        when(principal.getUserId())
            .thenReturn(userId);

        var auth = new UsernamePasswordAuthenticationToken(principal, "N/A", java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(authService.getCurrentUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("principal 타입이 다르면 UNSUPPORTED_PRINCIPAL 예외 반환")
    void getCurrentUserId_unsupportedPrincipal() {
        var auth = new UsernamePasswordAuthenticationToken("string-principal", "N/A", java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> authService.getCurrentUserId())
            .isInstanceOf(OotdException.class)
            .satisfies(ex ->
                assertThat(((OotdException) ex).getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_PRINCIPAL));
    }
}