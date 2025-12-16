package com.sprint.ootd5team.base.security.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.base.security.JwtInformation;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.user.dto.request.ResetPasswordRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 슬라이스 테스트")
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("CSRF 토큰 발급 엔드포인트는 204를 반환한다")
    @WithMockUser
    void getCsrfToken_returnsNoContent() throws Exception {
        mockMvc.perform(get("/api/auth/csrf-token"))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verifyNoMoreInteractions(authService, jwtTokenProvider);
    }

    @Test
    @DisplayName("refresh는 쿠키의 REFRESH_TOKEN으로 재발급하고 Set-Cookie 헤더와 JwtDto를 반환한다")
    @WithMockUser
    void refresh_setsCookie_andReturnsJwtDto() throws Exception {
        // given
        String refreshToken = "refresh-token-value";
        String newRefreshToken = "new-refresh-token-value";
        String accessToken = "new-access-token-value";

        JwtInformation info = org.mockito.Mockito.mock(JwtInformation.class);
        given(info.getRefreshToken()).willReturn(newRefreshToken);
        given(info.getAccessToken()).willReturn(accessToken);
        given(info.getUserDto()).willReturn(null);

        given(authService.refreshToken(refreshToken)).willReturn(info);

        ResponseCookie cookie = ResponseCookie.from("REFRESH_TOKEN", newRefreshToken)
            .httpOnly(true)
            .path("/")
            .build();
        given(jwtTokenProvider.generateRefreshTokenCookie(newRefreshToken)).willReturn(cookie);

        // when & then
        mockMvc.perform(
                post("/api/auth/refresh")
                    .with(csrf())
                    .cookie(new jakarta.servlet.http.Cookie("REFRESH_TOKEN", refreshToken))
            )
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", cookie.toString()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value(accessToken))
            .andExpect(jsonPath("$.userDto").doesNotExist());

        verify(authService).refreshToken(refreshToken);
        verify(jwtTokenProvider).generateRefreshTokenCookie(newRefreshToken);
    }

    @Test
    @DisplayName("reset-password는 AuthService에 요청을 위임하고 204를 반환한다")
    @WithMockUser
    void resetPassword_delegates_andReturnsNoContent() throws Exception {
        // given
        ResetPasswordRequest request = org.mockito.Mockito.mock(ResetPasswordRequest.class);
        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(
                post("/api/auth/reset-password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        ArgumentCaptor<ResetPasswordRequest> captor = ArgumentCaptor.forClass(ResetPasswordRequest.class);
        verify(authService).resetPassword(captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }
}