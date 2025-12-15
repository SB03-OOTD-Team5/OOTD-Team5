package com.sprint.ootd5team.base.security.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.OotdSecurityUserDetails;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtLoginSuccessHandler 단위 테스트")
public class JwtLoginSuccessHandlerTest {

    @Mock
    ObjectMapper objectMapper;

    @Mock
    JwtTokenProvider tokenProvider;

    @Mock
    JwtRegistry jwtRegistry;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    Authentication authentication;

    @InjectMocks
    JwtLoginSuccessHandler handler;

    private StringWriter body;
    private PrintWriter writer;
    private UserDto userDto;

    @BeforeEach
    void setUp() throws Exception {
        body = new StringWriter();
        writer = new PrintWriter(body);
        when(response.getWriter()).thenReturn(writer);

        userDto = new UserDto(
            UUID.randomUUID(),
            Instant.now(),
            null,
            null,
            null,
            null,
            false
        );
    }

    @Test
    @DisplayName("로그인 성공 시 토큰 생성/등록, refresh 쿠키 설정, 200 및 JwtDto JSON 응답")
    void onAuthenticationSuccess_success() throws Exception {
        // given
        OotdSecurityUserDetails userDetails = mock(OotdSecurityUserDetails.class);
        when(authentication.getPrincipal())
            .thenReturn(userDetails);

        when(userDetails.getUserDto())
            .thenReturn(userDto);

        when(userDetails.getUsername())
            .thenReturn("user@test.com");

        when(tokenProvider.generateAccessToken(any()))
            .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any()))
            .thenReturn("refresh-token");

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", "refresh-token")
            .httpOnly(true)
            .path("/")
            .build();

        when(tokenProvider.generateRefreshTokenCookie("refresh-token"))
            .thenReturn(refreshCookie);

        when(objectMapper.writeValueAsString(any()))
            .thenReturn("{\"accessToken\":\"access-token\"}");

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/json");
        verify(response).addHeader("Set-Cookie", refreshCookie.toString());
        verify(response).setStatus(HttpServletResponse.SC_OK);

        ArgumentCaptor<JwtInformation> captor = ArgumentCaptor.forClass(JwtInformation.class);
        verify(jwtRegistry).registerJwtInformation(captor.capture());
        JwtInformation info = captor.getValue();

        assertThat(info.getAccessToken()).isEqualTo("access-token");
        assertThat(info.getRefreshToken()).isEqualTo("refresh-token");

        writer.flush();
        assertThat(body.toString()).contains("access-token");
    }

    @Test
    @DisplayName("JOSEException 발생 시 500과 ErrorResponse JSON 반환")
    void onAuthenticationSuccess_joseException_returns500() throws Exception {
        // given
        OotdSecurityUserDetails userDetails = mock(OotdSecurityUserDetails.class);
        when(authentication.getPrincipal())
            .thenReturn(userDetails);

        when(userDetails.getUserDto())
            .thenReturn(userDto);
        when(userDetails.getUsername())
            .thenReturn("user@test.com");

        when(tokenProvider.generateAccessToken(any()))
            .thenThrow(new JOSEException("fail"));

        when(objectMapper.writeValueAsString(any()))
            .thenReturn("{\"error\":\"Token generation failed\"}");

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        writer.flush();
        assertThat(body.toString()).contains("Token generation failed");

        verify(jwtRegistry, never()).registerJwtInformation(any());
        verify(response, never()).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    @DisplayName("principal이 OotdSecurityUserDetails가 아니면 401과 ErrorResponse JSON 반환")
    void onAuthenticationSuccess_invalidPrincipal_returns401() throws Exception {
        // given
        when(authentication.getPrincipal())
            .thenReturn("not-user-details");
        when(objectMapper.writeValueAsString(any()))
            .thenReturn("{\"error\":\"유효하지않은 UserDetails 입니다.\"}");

        // when
        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writer.flush();
        assertThat(body.toString()).contains("유효하지않은 UserDetails");

        verify(jwtRegistry, never()).registerJwtInformation(any());
        verify(tokenProvider, never()).generateAccessToken(any());
        verify(tokenProvider, never()).generateRefreshToken(any());
        verify(response, never()).addHeader(eq("Set-Cookie"), anyString());
    }
}