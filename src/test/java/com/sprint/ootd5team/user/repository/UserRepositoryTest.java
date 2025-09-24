package com.sprint.ootd5team.user.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.domain.user.repository.UserRepositoryCustom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@EnableJpaAuditing
@ActiveProfiles("test")
@Import({QuerydslConfig.class})
@DisplayName("User Repository 단위 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepositoryCustom userRepositoryQueryDsl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        testUser1 = new User("Admin User", "admin@test.com", "password123", Role.ADMIN);
        testUser2 = new User("Regular User", "user@test.com", "password456", Role.USER);
        testUser3 = new User("Locked User", "locked@test.com", "password789", Role.USER);

        // testUser3을 잠금 상태로 설정 (리플렉션 또는 별도 메서드 필요)
        setLockedState(testUser3, true);

        entityManager.persistAndFlush(testUser1);
        // createdAt이 다르게 설정되도록 약간의 지연
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        entityManager.persistAndFlush(testUser2);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        entityManager.persistAndFlush(testUser3);
        entityManager.clear();
    }

    // User 엔티티에 setLocked 메서드가 없으므로 리플렉션 사용
    private void setLockedState(User user, boolean locked) {
        try {
            var field = User.class.getDeclaredField("locked");
            field.setAccessible(true);
            field.set(user, locked);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set locked state", e);
        }
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void findByEmail_Success() {
        // When
        Optional<User> result = userRepository.findByEmail("admin@test.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@test.com");
        assertThat(result.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.get().getName()).isEqualTo("Admin User");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 사용자 조회 - 빈 Optional 반환")
    void findByEmail_NotFound() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@test.com");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재함")
    void existsByEmail_Exists() {
        // When
        boolean exists = userRepository.existsByEmail("user@test.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않음")
    void existsByEmail_NotExists() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@test.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 이메일 오름차순")
    void findUsersWithCursor_EmailAscending() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@test.com");
        assertThat(result.get(1).getEmail()).isEqualTo("locked@test.com");
        assertThat(result.get(2).getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 이메일 내림차순")
    void findUsersWithCursor_EmailDescending() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "DESCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getEmail()).isEqualTo("user@test.com");
        assertThat(result.get(1).getEmail()).isEqualTo("locked@test.com");
        assertThat(result.get(2).getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 생성일 오름차순")
    void findUsersWithCursor_CreatedAtAscending() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "createdAt", "ASCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSize(3);
        // 첫 번째로 생성된 testUser1이 첫 번째에 와야 함
        assertThat(result.get(0).getId()).isEqualTo(testUser1.getId());
        assertThat(result.get(1).getId()).isEqualTo(testUser2.getId());
        assertThat(result.get(2).getId()).isEqualTo(testUser3.getId());
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 생성일 내림차순")
    void findUsersWithCursor_CreatedAtDescending() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "createdAt", "DESCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSize(3);
        // 마지막으로 생성된 testUser3가 첫 번째에 와야 함
        assertThat(result.get(0).getId()).isEqualTo(testUser3.getId());
        assertThat(result.get(1).getId()).isEqualTo(testUser2.getId());
        assertThat(result.get(2).getId()).isEqualTo(testUser1.getId());
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 이메일 필터링")
    void findUsersWithCursor_WithEmailFilter() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", "admin", null, null
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 대소문자 무시 이메일 필터링")
    void findUsersWithCursor_WithEmailFilterIgnoreCase() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", "ADMIN", null, null
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 역할 필터링 (USER)")
    void findUsersWithCursor_WithUserRoleFilter() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", null, "USER", null
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(user -> user.getRole() == Role.USER);
        assertThat(result).extracting(User::getEmail)
            .containsExactly("locked@test.com", "user@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 역할 필터링 (ADMIN)")
    void findUsersWithCursor_WithAdminRoleFilter() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", null, "ADMIN", null
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRole()).isEqualTo(Role.ADMIN);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 잠금 상태 필터링 (false)")
    void findUsersWithCursor_WithUnlockedFilter() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", null, null, false
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(user -> !user.getLocked());
        assertThat(result).extracting(User::getEmail)
            .containsExactly("admin@test.com", "user@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 잠금 상태 필터링 (true)")
    void findUsersWithCursor_WithLockedFilter() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", null, null, true
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLocked()).isTrue();
        assertThat(result.get(0).getEmail()).isEqualTo("locked@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 커서와 함께 (이메일)")
    void findUsersWithCursor_WithEmailCursor() {
        // Given
        String cursor = "locked@test.com";
        UUID idAfter = testUser3.getId();

        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            cursor, idAfter, 10, "email", "ASCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        // locked@test.com 이상의 이메일만 포함되어야 함
        assertThat(result).allMatch(user -> user.getEmail().compareTo(cursor) >= 0);
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 커서와 함께 (생성일)")
    void findUsersWithCursor_WithCreatedAtCursor() {
        // Given
        String cursor = testUser2.getCreatedAt().toString();
        UUID idAfter = testUser2.getId();

        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            cursor, idAfter, 10, "createdAt", "ASCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        // cursor 시점 이후의 데이터만 포함되어야 함
        Instant cursorInstant = Instant.parse(cursor);
        assertThat(result).allMatch(user -> !user.getCreatedAt().isBefore(cursorInstant));
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 제한된 개수")
    void findUsersWithCursor_WithLimit() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 2, "email", "ASCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("admin@test.com");
        assertThat(result.get(1).getEmail()).isEqualTo("locked@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 복합 필터링")
    void findUsersWithCursor_MultipleFilters() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", "test", "USER", false
        );

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("user@test.com");
        assertThat(result.get(0).getRole()).isEqualTo(Role.USER);
        assertThat(result.get(0).getLocked()).isFalse();
    }

    @Test
    @DisplayName("사용자 수 카운트 - 모든 사용자")
    void countUsers_All() {
        // When
        Long count = userRepositoryQueryDsl.countUsers(null, null, null);

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("사용자 수 카운트 - 역할별 (ADMIN)")
    void countUsers_ByAdminRole() {
        // When
        Long count = userRepositoryQueryDsl.countUsers("ADMIN", null, null);

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 수 카운트 - 역할별 (USER)")
    void countUsers_ByUserRole() {
        // When
        Long count = userRepositoryQueryDsl.countUsers("USER", null, null);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("사용자 수 카운트 - 이메일 필터링")
    void countUsers_ByEmail() {
        // When
        Long adminCount = userRepositoryQueryDsl.countUsers(null, "admin", null);
        Long testCount = userRepositoryQueryDsl.countUsers(null, "test", null);

        // Then
        assertThat(adminCount).isEqualTo(1L);
        assertThat(testCount).isEqualTo(3L); // 모든 테스트 이메일에 "test" 포함
    }

    @Test
    @DisplayName("사용자 수 카운트 - 잠금 상태별 (false)")
    void countUsers_ByUnlocked() {
        // When
        Long count = userRepositoryQueryDsl.countUsers(null, null, false);

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("사용자 수 카운트 - 잠금 상태별 (true)")
    void countUsers_ByLocked() {
        // When
        Long count = userRepositoryQueryDsl.countUsers(null, null, true);

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자 수 카운트 - 복합 필터링")
    void countUsers_MultipleFilters() {
        // When
        Long count = userRepositoryQueryDsl.countUsers("USER", "test", false);

        // Then
        assertThat(count).isEqualTo(1L); // user@test.com만 해당 (잠금되지 않은 USER)
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 빈 결과")
    void findUsersWithCursor_EmptyResult() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", "nonexistent", null, null
        );

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 잘못된 정렬 필드")
    void findUsersWithCursor_InvalidSortField() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "invalidField", "ASCENDING", null, null, null
        );

        // Then
        assertThat(result).hasSize(3); // 기본적으로 email로 정렬됨
        // 이메일 순으로 정렬되어야 함
        assertThat(result.get(0).getEmail()).isEqualTo("admin@test.com");
        assertThat(result.get(1).getEmail()).isEqualTo("locked@test.com");
        assertThat(result.get(2).getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("커서 페이지네이션으로 사용자 조회 - 빈 문자열 이메일 필터")
    void findUsersWithCursor_EmptyEmailFilter() {
        // When
        List<User> result = userRepositoryQueryDsl.findUsersWithCursor(
            null, null, 10, "email", "ASCENDING", "", null, null
        );

        // Then
        assertThat(result).hasSize(3); // 빈 문자열은 필터링하지 않음
    }


}
