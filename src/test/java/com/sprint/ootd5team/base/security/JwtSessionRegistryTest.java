package com.sprint.ootd5team.base.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sprint.ootd5team.domain.user.dto.UserDto;
import com.sprint.ootd5team.domain.user.entity.Role;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "securitytest"})
class JwtSessionRegistryTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private RedisLockProvider redisLockProvider;
    @Mock
    private ListOperations<String, Object> listOps;
    @Mock
    private SetOperations<String, Object> setOps;

    private RedisJwtRegistry redisJwtRegistry;

    private UUID userId;
    private JwtInformation jwtInfo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redisJwtRegistry = new RedisJwtRegistry(
            3, // maxActiveJwtCount
            jwtTokenProvider,
            eventPublisher,
            redisTemplate,
            redisLockProvider
        );

        userId = UUID.randomUUID();
        jwtInfo = new JwtInformation(
            new UserDto(userId, null, "test@example.com", "tester", Role.USER, null, false),
            "access-token-123",
            "refresh-token-123"
        );

        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
    }

    @Test
    @DisplayName("JwtRedisRegistry - Jwt 레지스트리에 토큰 저장 성공")
    void registerJwtInformation_success() {
        // given
        when(listOps.size(anyString())).thenReturn(0L);

        // when
        redisJwtRegistry.registerJwtInformation(jwtInfo);

        // then
        verify(redisLockProvider).acquireLock(userId.toString());
        verify(listOps).rightPush(eq("jwt:user:" + userId), eq(jwtInfo));
        verify(redisTemplate).expire(eq("jwt:user:" + userId), eq(Duration.ofMinutes(30)));
        verify(setOps).add("jwt:access_tokens", "access-token-123");
        verify(setOps).add("jwt:refresh_tokens", "refresh-token-123");
        verify(redisLockProvider).releaseLock(userId.toString());
    }

    @Test
    @DisplayName("JwtRedisRegistry - userId로 Jwt 토큰 무효화 성공")
    void invalidateJwtInformationByUserId_success() {
        // given
        when(listOps.range(anyString(), eq(0L), eq(-1L))).thenReturn(List.of(jwtInfo));

        // when
        redisJwtRegistry.invalidateJwtInformationByUserId(userId);

        // then
        verify(setOps).remove("jwt:access_tokens", "access-token-123");
        verify(setOps).remove("jwt:refresh_tokens", "refresh-token-123");
        verify(redisTemplate).delete("jwt:user:" + userId);
    }

    @Test
    @DisplayName("JwtRedisRegistry - userId로 Jwt 토큰 검색하여 유효성 확인 성공")
    void hasActiveJwtInformationByUserId_true() {
        // given
        when(listOps.size(anyString())).thenReturn(2L);

        // when
        boolean result = redisJwtRegistry.hasActiveJwtInformationByUserId(userId);

        // then
        assert(result);
    }

    @Test
    @DisplayName("JwtRedisRegistry - AccessToken으로 Jwt 토큰 유효성 확인 성공")
    void hasActiveJwtInformationByAccessToken_true() {
        when(setOps.isMember("jwt:access_tokens", "access-token-123")).thenReturn(true);

        assert(redisJwtRegistry.hasActiveJwtInformationByAccessToken("access-token-123"));
    }

    @Test
    @DisplayName("JwtRedisRegistry - Jwt 토큰 rotation 성공")
    void rotateJwtInformation_success() {
        JwtInformation newJwt = new JwtInformation(
            jwtInfo.getUserDto(),
            "new-access-token",
            "new-refresh-token"
        );

        when(listOps.range("jwt:user:" + userId, 0, -1))
            .thenReturn(List.of(jwtInfo));

        // when
        redisJwtRegistry.rotateJwtInformation("refresh-token-123", newJwt);

        // then
        verify(setOps).remove("jwt:access_tokens", "access-token-123");
        verify(setOps).remove("jwt:refresh_tokens", "refresh-token-123");
        verify(listOps).set(eq("jwt:user:" + userId), eq(0L), any(JwtInformation.class));
        verify(setOps).add("jwt:access_tokens", "new-access-token");
        verify(setOps).add("jwt:refresh_tokens", "new-refresh-token");
    }

    @Test
    @DisplayName("JwtRedisRegistry - 만료된 Jwt 제거 성공")
    void clearExpiredJwtInformation_removesExpired() {
        when(redisTemplate.keys("jwt:user:*"))
            .thenReturn(Set.of("jwt:user:" + userId));
        when(listOps.range("jwt:user:" + userId, 0, -1))
            .thenReturn(List.of(jwtInfo));

        // 만료된 토큰이라고 가정
        when(jwtTokenProvider.validateAccessToken("access-token-123")).thenReturn(false);
        when(jwtTokenProvider.validateRefreshToken("refresh-token-123")).thenReturn(false);

        // when
        redisJwtRegistry.clearExpiredJwtInformation();

        // then
        verify(setOps).remove("jwt:access_tokens", "access-token-123");
        verify(setOps).remove("jwt:refresh_tokens", "refresh-token-123");
        verify(redisTemplate).delete("jwt:user:" + userId);
    }
}