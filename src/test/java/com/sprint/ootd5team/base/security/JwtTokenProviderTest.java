package com.sprint.ootd5team.base.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.JOSEException;
import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.entity.Role;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "securitytest"})
public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private OotdSecurityUserDetails userDetails;

    @BeforeEach
    void setUp() throws JOSEException {
        String testAccessSecret = "test-access-secret-key-for-jwt-token-generation-and-validation-must-be-long-enough";
        String testRefreshSecret = "test-refresh-secret-key-for-jwt-token-generation-and-validation-must-be-long-enough";
        int testAccessExpirationMs = 1800000; // 30 분
        int testRefreshExpirationMs = 604800000; // 7 일

        jwtTokenProvider = new JwtTokenProvider(testAccessExpirationMs, testRefreshExpirationMs, testAccessSecret, testRefreshSecret);

        UUID userId = UUID.randomUUID();
        UserDto userDto = new UserDto(
            userId,
            Instant.now(),
            "test@example.com",
            "testuser",
            Role.USER,
            null,
            false
        );

        userDetails = new OotdSecurityUserDetails(userDto, "encoded-password");
    }

    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void generateAccessToken_Success() throws JOSEException {
        // When
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT should have 3 parts: header.payload.signature
    }

    @Test
    @DisplayName("유효한 JWT 토큰 검증 테스트")
    void validateToken_ValidAccessToken_ReturnsTrue() throws JOSEException {
        // Given
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // When
        boolean isValid = jwtTokenProvider.validateAccessToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 검증 테스트")
    void validateToken_InvalidAccessToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtTokenProvider.validateAccessToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 테스트")
    void validateToken_NullAccessToken_ReturnsFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateAccessToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("빈 토큰 검증 테스트")
    void validateToken_EmptyAccessToken_ReturnsFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateAccessToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("JWT 토큰에서 사용자명 추출 테스트")
    void getUsernameFromToken_ValidToken_ReturnsUsername() throws JOSEException {
        // Given
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // When
        String username = jwtTokenProvider.getEmailFromToken(token);

        // Then
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("잘못된 토큰에서 사용자명 추출 테스트 - 예외 발생")
    void getUsernameFromToken_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getEmailFromToken(invalidToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid JWT token");
    }

    @Test
    @DisplayName("JWT 토큰에서 토큰 ID 추출 테스트")
    void getTokenId_ValidToken_ReturnsTokenId() throws JOSEException {
        // Given
        String token = jwtTokenProvider.generateAccessToken(userDetails);

        // When
        String tokenId = jwtTokenProvider.getTokenId(token);

        // Then
        assertThat(tokenId).isNotNull();
        assertThat(tokenId).isNotEmpty();
        // UUID format check
        assertThat(UUID.fromString(tokenId)).isNotNull();
    }

    @Test
    @DisplayName("잘못된 토큰에서 토큰 ID 추출 테스트 - 예외 발생")
    void getTokenId_InvalidToken_ThrowsException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getTokenId(invalidToken))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid JWT token");
    }

    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    void validateToken_ExpiredAccessToken_ReturnsFalse() throws JOSEException {
        // Given
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
            1,
            604800000,
            "test-access-secret-key-for-jwt-token-generation-and-validation-must-be-long-enough",
            "test-refresh-secret-key-for-jwt-token-generation-and-validation-must-be-long-enough"
        );

        String token = shortExpirationProvider.generateAccessToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = shortExpirationProvider.validateAccessToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("다른 사용자의 토큰 생성 및 검증 테스트")
    void generateAccessToken_DifferentUser_HasDifferentClaims() throws JOSEException {
        // Given
        UUID anotherUserId = UUID.randomUUID();
        UserDto anotherUserDto = new UserDto(
            anotherUserId,
            Instant.now(),
            "another@example.com",
            "anotheruser",
            Role.ADMIN,
            null,
            false
        );
        OotdSecurityUserDetails anotherUserDetails = new OotdSecurityUserDetails(anotherUserDto,
            "another-password");

        // When
        String token1 = jwtTokenProvider.generateAccessToken(userDetails);
        String token2 = jwtTokenProvider.generateAccessToken(anotherUserDetails);

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getEmailFromToken(token1)).isEqualTo("test@example.com");
        assertThat(jwtTokenProvider.getEmailFromToken(token2)).isEqualTo("another@example.com");
        assertThat(jwtTokenProvider.getTokenId(token1)).isNotEqualTo(
            jwtTokenProvider.getTokenId(token2));
    }
}
