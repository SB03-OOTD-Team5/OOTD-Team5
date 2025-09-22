package com.sprint.ootd5team.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.entity.FeedClothes;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.impl.FeedClothesRepositoryImpl;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({FeedClothesRepositoryImpl.class, QuerydslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
@DisplayName("FeedRepositoryImpl 테스트")
public class FeedRepositoryImplTest {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private FeedClothesRepository feedClothesRepository;

    @Autowired
    private EntityManager em;

    private UUID feedId;

    @BeforeEach
    void setUp() {
        feedId = UUID.randomUUID();

        User owner = new User(
            "테스트유저",
            "test@example.com",
            "password",
            Role.USER
        );
        em.persist(owner);

        Clothes clothes = Clothes.builder()
            .name("아디다스 트레이닝 팬츠")
            .type(ClothesType.BOTTOM)
            .imageUrl("https://image.url/adidasPants.jpg")
            .owner(owner)
            .build();
        em.persist(clothes);

        FeedClothes feedClothes = new FeedClothes(feedId, clothes.getId());
        em.persist(feedClothes);

        ClothesAttribute attr = new ClothesAttribute("색상");
        em.persist(attr);

        ClothesAttributeDef attrDef = new ClothesAttributeDef(attr, "초록");
        em.persist(attrDef);

        ClothesAttributeValue cav = new ClothesAttributeValue(clothes, attr, "초록");
        em.persist(cav);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("피드 Id 리스트로 ootds 목록 조회 성공")
    void findOotdsByFeedIds_success() {
        // when
        Map<UUID, List<OotdDto>> result = feedClothesRepository.findOotdsByFeedIds(List.of(feedId));

        // then
        assertThat(result).containsKey(feedId);
        List<OotdDto> ootds = result.get(feedId);
        assertThat(ootds).hasSize(1);

        OotdDto ootd = ootds.get(0);
        assertThat(ootd.name()).isEqualTo("아디다스 트레이닝 팬츠");
        assertThat(ootd.type()).isEqualTo("BOTTOM");
        assertThat(ootd.attributes()).hasSize(1);
        assertThat(ootd.attributes().get(0).value()).isEqualTo("초록");
    }

    @Test
    @DisplayName("Feed 삭제 시 Feed 엔티티가 삭제 성공")
    void deleteFeed_onlyFeedCheckedInH2() {
        Feed feed = new Feed(UUID.randomUUID(), UUID.randomUUID(), "내용", 0, 0);
        feedRepository.save(feed);

        UUID feedId = feed.getId();
        feedRepository.delete(feed);
        feedRepository.flush();

        assertThat(feedRepository.findById(feedId)).isEmpty();
    }

    @Test
    @DisplayName("FeedId와 UserId로 FeedDto 단건 조회 성공")
    void findFeedDtoById_success() {
        // given
        User user = new User("작성자", "author@example.com", "password", Role.USER);
        em.persist(user);

        Profile profile = new Profile(
            user.getId(),
            "닉네임",
            null, null,
            null, null, null, null, null, null,
            2
        );
        em.persist(profile);

        Weather weather = Weather.builder()
            .forecastedAt(Instant.now())
            .forecastAt(Instant.now())
            .skyStatus(SkyStatus.CLEAR)
            .latitude(BigDecimal.ONE)
            .longitude(BigDecimal.ONE)
            .precipitationType(PrecipitationType.NONE)
            .temperature(20.0)
            .temperatureMin(18.0)
            .temperatureMax(25.0)
            .profile(profile)
            .build();
        em.persist(weather);

        Feed feed = new Feed(user.getId(), weather.getId(), "테스트 피드", 0, 0);
        em.persist(feed);

        em.flush();
        em.clear();

        // when
        FeedDto dto = feedRepository.findFeedDtoById(feed.getId(), user.getId());

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(feed.getId());
        assertThat(dto.content()).isEqualTo("테스트 피드");
        assertThat(dto.author().userId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("존재하지 않는 FeedId로 FeedDto 조회 시 null 반환")
    void findFeedDtoById_notFound() {
        // when
        FeedDto result = feedRepository.findFeedDtoById(UUID.randomUUID(), UUID.randomUUID());

        // then
        assertThat(result).isNull();
    }

}