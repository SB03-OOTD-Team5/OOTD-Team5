package com.sprint.ootd5team.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepositoryImpl;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(QuerydslConfig.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@ActiveProfiles("test")
@DisplayName("ClothesRepositoryImpl 테스트")
class ClothesRepositoryImplTest {

    @Autowired
    private ClothesRepositoryImpl clothesRepository;

    @Autowired
    private EntityManager em;

    private User owner;
    private UUID ownerId;
    private Clothes clothes;

    @BeforeEach
    void setUp() {
        owner = new User("쪼쪼", "zzo@email.com", "zzo1234!", Role.USER);
        ReflectionTestUtils.setField(owner, "createdAt", Instant.parse("2024-01-01T00:00:00Z"));
        em.persist(owner);
        em.flush();
        ownerId = owner.getId();

        em.persist(makeClothes(owner, "흰 티셔츠", ClothesType.TOP, "2024-01-01T10:00:00Z"));
        em.persist(makeClothes(owner, "청바지", ClothesType.BOTTOM, "2024-01-01T09:00:00Z"));
        em.persist(makeClothes(owner, "운동화", ClothesType.SHOES, "2024-01-01T08:00:00Z"));

        clothes = makeClothes(owner, "운동화2", ClothesType.SHOES, "2024-01-01T08:00:00Z");
        em.persist(clothes);
        em.flush();
        em.clear();
    }

    private Clothes makeClothes(User owner, String name, ClothesType type, String createdAt) {
        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name(name)
            .type(type)
            .imageUrl(null)
            .build();
        ReflectionTestUtils.setField(clothes, "createdAt", Instant.parse(createdAt));
        return clothes;
    }

    @Test
    void TOP타입에_해당하는_결과_반환() {
        // given
        ClothesType type = ClothesType.TOP;

        // when
        List<Clothes> result = clothesRepository.findClothes(ownerId, type, null, null, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("흰 티셔츠");
    }

    @Test
    void createdAtCursor로_다음페이지조회() {
        // given
        Instant after = Instant.parse("2024-01-01T09:00:00Z");

        // when
        List<Clothes> result = clothesRepository.findClothes(
            ownerId,
            null,
            after,
            null,
            10
        );

        // then
        assertThat(result)
            .isNotEmpty()
            .extracting(Clothes::getName)
            .contains("운동화", "운동화2");
    }

    @Test
    void createdAtCursor가_같으면_id값을_기준_다음페이지조회() {
        // given
        Instant cursor = Instant.parse("2024-01-01T08:00:00Z");
        UUID idAfter = clothes.getId(); // "운동화2"

        // when
        List<Clothes> result = clothesRepository.findClothes(
            ownerId,
            null,
            cursor,
            idAfter,
            10
        );

        // then
        assertThat(result.get(0).getCreatedAt()).isEqualTo(cursor);
    }
}