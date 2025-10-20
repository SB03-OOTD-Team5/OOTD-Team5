package com.sprint.ootd5team.domain.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepositoryImpl;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.entity.FeedClothes;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Limit;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({ClothesRepositoryImpl.class, QuerydslConfig.class, JpaAuditingConfig.class})
@DisplayName("RecommendationRepository 테스트")
@ActiveProfiles("test")
class RecommendationRepositoryTest {

    @Autowired
    private ClothesRepository clothesRepository;

    @Autowired
    private EntityManager em;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User("테스터", "tester@example.com", "password", Role.USER);
        em.persist(owner);
    }

    @Test
    @DisplayName("날씨 조건으로 추천 의상을 조회한다")
    void 날씨_조건으로_추천_의상을_조회한다() {
        // given
        UUID targetWeatherId = UUID.randomUUID();
        Clothes expected = persistClothesWithFeed(targetWeatherId, "블루종", ClothesType.OUTER);
        UUID expectedId = expected.getId();
        persistClothesWithFeed(UUID.randomUUID(), "화이트셔츠", ClothesType.TOP);
        em.flush();
        em.clear();

        // when
        List<Clothes> result = clothesRepository.findClothesInWeatherIds(List.of(targetWeatherId));

        // then
        assertThat(result).extracting(Clothes::getId)
            .containsExactly(expectedId);
    }

    @Test
    @DisplayName("추천 제외 목록을 기반으로 랜덤 의상을 조회한다")
    void 추천_제외_목록을_기반으로_랜덤_의상을_조회한다() {
        // given
        UUID weatherId = UUID.randomUUID();
        Clothes clothesA = persistClothesWithFeed(weatherId, "카디건", ClothesType.OUTER);
        Clothes clothesB = persistClothesWithFeed(weatherId, "슬랙스", ClothesType.BOTTOM);
        Clothes clothesC = persistClothesWithFeed(weatherId, "스니커즈", ClothesType.SHOES);
        UUID clothesAId = clothesA.getId();
        UUID clothesBId = clothesB.getId();
        UUID clothesCId = clothesC.getId();
        em.flush();
        em.clear();

        // when
        List<Clothes> result = clothesRepository.findByIdNotIn(
            List.of(clothesB.getId()),
            Limit.of(10)
        );

        // then
        assertThat(result).extracting(Clothes::getId)
            .contains(clothesAId, clothesCId)
            .doesNotContain(clothesBId);
    }

    @Test
    @DisplayName("추천 의상 조회 시 중복을 제거한다")
    void 추천_의상_조회시_중복을_제거한다() {
        // given
        UUID weatherId = UUID.randomUUID();
        Clothes duplicated = persistClothesWithFeed(weatherId, "가죽자켓", ClothesType.OUTER);
        UUID duplicatedId = duplicated.getId();
        Feed secondFeed = Feed.of(owner.getId(), weatherId, "두번째 피드");
        em.persist(secondFeed);
        em.persist(new FeedClothes(secondFeed.getId(), duplicated.getId()));
        em.flush();
        em.clear();

        // when
        List<Clothes> result = clothesRepository.findClothesInWeatherIds(List.of(weatherId));

        // then
        assertThat(result).extracting(Clothes::getId)
            .containsExactly(duplicatedId);
    }

    private Clothes persistClothesWithFeed(UUID weatherId, String name, ClothesType type) {
        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name(name)
            .type(type)
            .build();
        em.persist(clothes);

        Feed feed = Feed.of(owner.getId(), weatherId, name + " 후기");
        em.persist(feed);

        FeedClothes feedClothes = new FeedClothes(feed.getId(), clothes.getId());
        em.persist(feedClothes);

        return clothes;
    }
}
