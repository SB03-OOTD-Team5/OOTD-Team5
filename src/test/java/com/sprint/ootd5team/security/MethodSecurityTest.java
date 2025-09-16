package com.sprint.ootd5team.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.ootd5team.base.security.JwtAuthenticationFilter;
import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.base.security.JwtTokenProvider;
import com.sprint.ootd5team.base.security.RedisLockProvider;
import com.sprint.ootd5team.base.security.service.AuthService;
import com.sprint.ootd5team.domain.user.dto.request.UserLockUpdateRequest;
import com.sprint.ootd5team.domain.user.dto.request.UserRoleUpdateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * 메서드 보안(@PreAuthorize) 검증 테스트: 서비스 레이어의 보안 어노테이션이 실제로 동작하는지 확인한다.
 *  *  - USER 역할로 관리자 전용 메서드 호출 -> AccessDeniedException
 *  *  - ADMIN 역할로 호출 -> 정상 실행 (여기서는 예외 미발생까지만 확인)
 */
@SpringBootTest
@ActiveProfiles({"test", "securitytest"})
public class MethodSecurityTest {

    @Autowired
    AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JwtRegistry jwtRegistry;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RedisLockProvider redisLockProvider;

    private UUID userId;
    @MockitoBean
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;


    @BeforeEach
    void init(){
        User user = new User("test", "test@test.com", "qwe123", Role.USER);
        userId = userRepository.save(user).getId();
    }


    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("USER 권한으로 역할 업데이트 메서드 호출시 접근 거부")
    void user_cannot_access_admin_method1() {
        assertThatThrownBy(() ->
           authService.updateRoleInternal(userId,new UserRoleUpdateRequest(Role.ADMIN.name()))
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("USER 권한으로 계정 잠금 메서드 호출시 접근 거부")
    void user_cannot_access_admin_method2() {

    }
}
