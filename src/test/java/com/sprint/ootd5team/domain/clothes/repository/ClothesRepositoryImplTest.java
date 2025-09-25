package com.sprint.ootd5team.domain.clothes.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepositoryImpl;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Import(QuerydslConfig.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Sql(scripts = {"classpath:user-data.sql", "classpath:clothes-data.sql"})
@ActiveProfiles("test")
@DisplayName("ClothesRepositoryImpl 슬라이스 테스트")
class ClothesRepositoryImplTest {

    private static final UUID ownerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    @Autowired
    private ClothesRepositoryImpl clothesRepository;
    @Autowired
    private EntityManager em;

    @Test
    void TOP타입에_해당하는_결과_반환() {
        // given
        ClothesType type = ClothesType.TOP;

        // when
        List<Clothes> result = clothesRepository.findByOwnerWithCursor(ownerId, type, null, null,
            10,
            Direction.DESC);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("흰 티셔츠");
    }

    @Test
    void createdAtCursor로_다음페이지조회() {
        // given
        Instant after = Instant.parse("2024-01-01T09:00:00Z");

        // when
        List<Clothes> result = clothesRepository.findByOwnerWithCursor(
            ownerId,
            null,
            after,
            null,
            10,
            Direction.DESC
        );

        // then
        assertThat(result)
            .isNotEmpty()
            .extracting(Clothes::getName)
            .containsExactlyInAnyOrder("운동화1", "운동화2", "운동화3");
    }

    @Test
    void createdAtCursor가_같으면_id값을_기준으로_다음페이지를_조회한다_DESC() {
        // given
        Instant cursor = Instant.parse("2024-01-01T08:00:00Z");
        UUID idAfter = UUID.fromString("22222222-2222-2222-2222-222222222222"); // 운동화2

        // when
        List<Clothes> result = clothesRepository.findByOwnerWithCursor(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            null,
            cursor,
            idAfter,
            10,
            Direction.DESC
        );

        // then
        assertThat(result).extracting(Clothes::getId)
            .containsExactly(UUID.fromString("11111111-1111-1111-1111-111111111111")); // 운동화1
    }

    @Test
    void createdAtCursor가_같으면_id값을_기준으로_다음페이지를_조회한다_ASC() {
        // given
        Instant cursor = Instant.parse("2024-01-01T08:00:00Z");
        UUID idAfter = UUID.fromString("11111111-1111-1111-1111-111111111111"); // 운동화1

        // when
        List<Clothes> result = clothesRepository.findByOwnerWithCursor(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            null,
            cursor,
            idAfter,
            10,
            Direction.ASC
        );

        // then
        assertThat(result).extracting(Clothes::getId)
            .containsExactlyInAnyOrder(
                UUID.fromString("22222222-2222-2222-2222-222222222222"), // 운동화2
                UUID.fromString("33333333-3333-3333-3333-333333333333"),  // 운동화3
                UUID.fromString("bbbbbbbb-0000-0000-0000-aaaaaaaaaaaa"), // 청바지
                UUID.fromString("aaaaaaaa-0000-0000-0000-aaaaaaaaaaaa")  // 흰 티셔츠
            );
    }
}