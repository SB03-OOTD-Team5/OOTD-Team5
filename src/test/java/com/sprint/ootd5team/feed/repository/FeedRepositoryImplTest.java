package com.sprint.ootd5team.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.base.exception.feed.InvalidSortOptionException;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.entity.FeedClothes;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.impl.FeedClothesRepositoryImpl;
import com.sprint.ootd5team.domain.location.entity.Location;
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

        User owner = createUser("테스트유저", "test@example.com");

        Clothes clothes = Clothes.builder()
            .name("아디다스 트레이닝 팬츠")
            .type(ClothesType.BOTTOM)
            .imageUrl("https://image.url/adidasPants.jpg")
            .owner(owner)
            .build();
        em.persist(clothes);

        em.persist(new FeedClothes(feedId, clothes.getId()));

        ClothesAttribute attr = new ClothesAttribute("색상");
        em.persist(attr);
        em.persist(new ClothesAttributeDef(attr, "초록"));
        em.persist(new ClothesAttributeValue(clothes, attr, "초록"));

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
        User user = createUser("작성자", "author@example.com");
        Profile profile = createProfile(user);
        Weather weather = createWeather(profile, SkyStatus.CLEAR, PrecipitationType.NONE);

        Feed feed = createFeed(user, weather, "테스트 피드");
        persistAndClear(user, profile, weather, feed);

        FeedDto dto = feedRepository.findFeedDtoById(feed.getId(), user.getId());

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

    @Test
    @DisplayName("FeedListRequest 조건으로 FeedDto 목록 조회 성공")
    void findFeedDtos_success() {
        // given
        User user = createUser("작성자2", "author2@example.com");
        Profile profile = createProfile(user);
        Weather weather = createWeather(profile, SkyStatus.CLEAR, PrecipitationType.NONE);

        Feed feed = createFeed(user, weather, "검색 키워드 포함 피드");
        persistAndClear(user, profile, weather, feed);

        FeedListRequest request = new FeedListRequest(
            null, null, 10, "createdAt",
            SortDirection.DESCENDING, "키워드", SkyStatus.CLEAR, PrecipitationType.NONE, user.getId()
        );

        // when
        List<FeedDto> result = feedRepository.findFeedDtos(request, user.getId());

        // then
        assertThat(result).hasSize(1);
        FeedDto dto = result.get(0);
        assertThat(dto.content()).contains("검색 키워드");
        assertThat(dto.author().userId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("조건에 맞는 피드 개수 조회 성공")
    void countFeeds_success() {
        // given
        User user = createUser("작성자3", "author3@example.com");
        Profile profile = createProfile(user);
        Weather weather = createWeather(profile, SkyStatus.CLEAR, PrecipitationType.RAIN);

        Feed feed1 = createFeed(user, weather, "비 오는 날 피드");
        Feed feed2 = createFeed(user, weather, "맑은 하늘 피드");
        persistAndClear(user, profile, weather, feed1, feed2);

        // when
        long countAll = feedRepository.countFeeds(null, null, null, null);
        long countRain = feedRepository.countFeeds(null, null, PrecipitationType.RAIN, null);
        long countByAuthor = feedRepository.countFeeds(null, null, null, user.getId());

        // then
        assertThat(countAll).isGreaterThanOrEqualTo(2);
        assertThat(countRain).isEqualTo(2);
        assertThat(countByAuthor).isEqualTo(2);
    }

    @Test
    @DisplayName("잘못된 sortBy 값으로 조회 시 예외 발생")
    void findFeedDtos_invalidSortBy() {
        FeedListRequest request = new FeedListRequest(
            null, null, 0,
            "invalidSort", SortDirection.DESCENDING,
            null, null, null, null
        );

        assertThatThrownBy(() -> feedRepository.findFeedDtos(request, UUID.randomUUID()))
            .isInstanceOf(InvalidSortOptionException.class);
    }

    @Test
    @DisplayName("createdAt ASC + cursor 조건으로 FeedDto 목록 조회 성공")
    void findFeedDtos_createdAtAsc_withCursor() throws InterruptedException {
        User user = createUser("작성자", "asc@test.com");
        Profile profile = createProfile(user);
        Weather weather = createWeather(profile, SkyStatus.CLEAR, PrecipitationType.NONE);

        Feed feed1 = createFeed(user, weather, "피드1");
        Thread.sleep(5);
        Feed feed2 = createFeed(user, weather, "피드2");
        em.flush();
        em.clear();

        String cursor = feed1.getCreatedAt().toString();
        UUID idAfter = feed1.getId();

        FeedListRequest request = new FeedListRequest(
            cursor, idAfter, 10,
            "createdAt", SortDirection.ASCENDING,
            null, null, null, null
        );

        List<FeedDto> result = feedRepository.findFeedDtos(request, user.getId());

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("createdAt DESC + cursor 조건으로 FeedDto 목록 조회 성공")
    void findFeedDtos_createdAtDesc_withCursor() throws InterruptedException {
        User user = createUser("작성자", "desc@test.com");
        Profile profile = createProfile(user);
        Weather weather = createWeather(profile, SkyStatus.CLEAR, PrecipitationType.NONE);

        Feed older = createFeed(user, weather, "이전 피드");
        Thread.sleep(5);
        Feed newer = createFeed(user, weather, "새 피드");
        em.flush();
        em.clear();

        String cursor = newer.getCreatedAt().toString();
        UUID idAfter = newer.getId();

        FeedListRequest request = new FeedListRequest(
            cursor, idAfter, 10,
            "createdAt", SortDirection.DESCENDING,
            null, null, null, null
        );

        List<FeedDto> result = feedRepository.findFeedDtos(request, user.getId());

        assertThat(result).extracting(FeedDto::id).contains(older.getId());
    }

    @Test
    @DisplayName("likeCount ASC + cursor 조건으로 FeedDto 목록 조회 성공")
    void findFeedDtos_likeCountAsc_withCursor() {
        User user = createUser("작성자", "ascLike@test.com");
        Profile profile = createProfile(user);
        Weather weather = createWeather(profile, SkyStatus.CLEAR, PrecipitationType.NONE);

        Feed lowLike = new Feed(user.getId(), weather.getId(), "좋아요 적음", 0, 5);
        Feed highLike = new Feed(user.getId(), weather.getId(), "좋아요 많음", 0, 10);
        em.persist(lowLike);
        em.persist(highLike);
        em.flush();
        em.clear();

        String cursor = String.valueOf(lowLike.getLikeCount());
        UUID idAfter = lowLike.getId();

        FeedListRequest request = new FeedListRequest(
            cursor, idAfter, 10,
            "likeCount", SortDirection.ASCENDING,
            null, null, null, null
        );

        List<FeedDto> result = feedRepository.findFeedDtos(request, user.getId());

        assertThat(result).extracting(FeedDto::id).contains(highLike.getId());
    }

    @Test
    @DisplayName("likeCount DESC + cursor 조건으로 FeedDto 목록 조회 성공")
    void findFeedDtos_likeCountDesc_withCursor() {
        User user = createUser("작성자", "descLike@test.com");
        Profile profile = createProfile(user);
        Weather weather = createWeather(profile, SkyStatus.CLEAR, PrecipitationType.NONE);

        Feed highLike = new Feed(user.getId(), weather.getId(), "좋아요 많음", 0, 10);
        Feed lowLike = new Feed(user.getId(), weather.getId(), "좋아요 적음", 0, 5);
        em.persist(highLike);
        em.persist(lowLike);
        em.flush();
        em.clear();

        String cursor = String.valueOf(highLike.getLikeCount());
        UUID idAfter = highLike.getId();

        FeedListRequest request = new FeedListRequest(
            cursor, idAfter, 10,
            "likeCount", SortDirection.DESCENDING,
            null, null, null, null
        );

        List<FeedDto> result = feedRepository.findFeedDtos(request, user.getId());

        assertThat(result).extracting(FeedDto::id).contains(lowLike.getId());
    }

    private User createUser(String name, String email) {
        User user = new User(name, email, "password", Role.USER);
        em.persist(user);
        return user;
    }

    private Profile createProfile(User user) {
        Location location = createLocation();
        Profile profile = new Profile(
            user, "닉네임", null, null, null, location, 2
        );
        em.persist(profile);
        return profile;
    }

    private Weather createWeather(Profile profile, SkyStatus skyStatus, PrecipitationType type) {
        Location location = profile.getLocation();
        Weather weather = Weather.builder()
            .forecastedAt(Instant.now())
            .forecastAt(Instant.now())
            .skyStatus(skyStatus)
            .precipitationType(type)
            .temperature(20.0)
            .temperatureMin(18.0)
            .temperatureMax(25.0)
            .location(location)
            .build();
        em.persist(weather);
        return weather;
    }

    private Feed createFeed(User user, Weather weather, String content) {
        Feed feed = new Feed(user.getId(), weather.getId(), content, 0, 0);
        em.persist(feed);
        return feed;
    }

    private Location createLocation() {
        Location location = Location.builder()
            .latitude(BigDecimal.valueOf(37.5665))
            .longitude(BigDecimal.valueOf(126.9780))
            .xCoord(60)
            .yCoord(127)
            .locationNames("서울특별시 중구")
            .locationCode("11B10101")
            .build();
        em.persist(location);
        return location;
    }

    private void persistAndClear(Object... entities) {
        for (Object entity : entities) {
            em.persist(entity);
        }
        em.flush();
        em.clear();
    }
}