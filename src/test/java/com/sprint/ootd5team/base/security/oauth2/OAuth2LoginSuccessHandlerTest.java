package com.sprint.ootd5team.base.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.base.exception.ErrorResponse;
import com.sprint.ootd5team.base.security.JwtDto;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test", "securitytest"})
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OAuth2LoginSuccessHandler successHandler;

    private UserDto testUserDto;
    private OotdOAuth2UserDetails oauthUserDetails;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 UserDto 생성
        testUserDto = new UserDto(
            UUID.randomUUID(),
            Instant.now(),
            "test@example.com",
            "테스트유저",
            Role.USER,
            List.of("google"),
            false
        );

        // OAuth2UserDetails Mock 생성
        oauthUserDetails = mock(OotdOAuth2UserDetails.class);

        // Response Writer 설정
        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 - 정상 처리")
    void onAuthenticationSuccess_Success() throws Exception {
        // Given
        String accessToken = "test.access.token";
        String refreshToken = "test.refresh.token";
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(7 * 24 * 60 * 60)
            .build();

        JwtDto expectedJwtDto = new JwtDto(testUserDto, accessToken);
        String expectedJson = "{\"userDto\":{},\"accessToken\":\"" + accessToken + "\"}";

        when(oauthUserDetails.getUserDto()).thenReturn(testUserDto);
        when(authentication.getPrincipal()).thenReturn(oauthUserDetails);
        when(tokenProvider.generateAccessToken(testUserDto)).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken(testUserDto)).thenReturn(refreshToken);
        when(tokenProvider.generateRefreshTokenCookie(refreshToken)).thenReturn(refreshCookie);
        when(objectMapper.writeValueAsString(any(JwtDto.class))).thenReturn(expectedJson);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(tokenProvider).generateAccessToken(testUserDto);
        verify(tokenProvider).generateRefreshToken(testUserDto);
        verify(jwtRegistry).registerJwtInformation(any(JwtInformation.class));
        verify(tokenProvider).generateRefreshTokenCookie(refreshToken);
        verify(response).addHeader("Set-Cookie", refreshCookie.toString());
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).sendRedirect("/#/recommendations");

        // 응답 본문 검증
        String responseBody = responseWriter.toString();
        assertThat(responseBody).contains(accessToken);
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 - JwtRegistry에 정보 등록 확인")
    void onAuthenticationSuccess_RegisterJwtInformation() throws Exception {
        // Given
        String accessToken = "test.access.token";
        String refreshToken = "test.refresh.token";
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken).build();

        when(oauthUserDetails.getUserDto()).thenReturn(testUserDto);
        when(authentication.getPrincipal()).thenReturn(oauthUserDetails);
        when(tokenProvider.generateAccessToken(testUserDto)).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken(testUserDto)).thenReturn(refreshToken);
        when(tokenProvider.generateRefreshTokenCookie(refreshToken)).thenReturn(refreshCookie);
        when(objectMapper.writeValueAsString(any(JwtDto.class))).thenReturn("{}");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(jwtRegistry).registerJwtInformation(argThat(jwtInfo ->
            jwtInfo.getUserDto().equals(testUserDto) &&
                jwtInfo.getAccessToken().equals(accessToken) &&
                jwtInfo.getRefreshToken().equals(refreshToken)
        ));
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - JWT 생성 실패 (JOSEException)")
    void onAuthenticationSuccess_JwtGenerationFailed() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(oauthUserDetails);
        when(tokenProvider.generateAccessToken(testUserDto))
            .thenThrow(new JOSEException("JWT generation failed"));

        String errorJson = "{\"error\":\"Token generation failed\"}";
        when(oauthUserDetails.getUserDto()).thenReturn(testUserDto);
        when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(errorJson);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(jwtRegistry, never()).registerJwtInformation(any());
        verify(response, never()).sendRedirect(anyString());

        String responseBody = responseWriter.toString();
        assertThat(responseBody).contains("Token generation failed");
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - RefreshToken 생성 실패")
    void onAuthenticationSuccess_RefreshTokenGenerationFailed() throws Exception {
        // Given
        String accessToken = "test.access.token";
        when(authentication.getPrincipal()).thenReturn(oauthUserDetails);
        when(oauthUserDetails.getUserDto()).thenReturn(testUserDto);
        when(tokenProvider.generateAccessToken(testUserDto)).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken(testUserDto))
            .thenThrow(new JOSEException("Refresh token generation failed"));

        String errorJson = "{\"error\":\"Token generation failed\"}";
        when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(errorJson);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(jwtRegistry, never()).registerJwtInformation(any());

        String responseBody = responseWriter.toString();
        assertThat(responseBody).contains("Token generation failed");
    }

    @Test
    @DisplayName("OAuth2 로그인 실패 - Principal이 OotdOAuth2UserDetails가 아님")
    void onAuthenticationSuccess_InvalidPrincipal() throws Exception {
        // Given
        Object invalidPrincipal = new Object();
        when(authentication.getPrincipal()).thenReturn(invalidPrincipal);

        String errorJson = "{\"error\":\"Authentication failed: Invalid user details\"}";
        when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn(errorJson);

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(tokenProvider, never()).generateAccessToken(any());
        verify(tokenProvider, never()).generateRefreshToken(any());
        verify(jwtRegistry, never()).registerJwtInformation(any());

        String responseBody = responseWriter.toString();
        assertThat(responseBody).contains("Invalid user details");
    }
}