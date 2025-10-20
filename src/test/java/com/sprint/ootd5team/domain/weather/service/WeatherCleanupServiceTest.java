package com.sprint.ootd5team.domain.weather.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.base.util.DateTimeUtils;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@Import({WeatherCleanupService.class, JpaAuditingConfig.class, QuerydslConfig.class})
@TestPropertySource(properties = "spring.sql.init.mode=never")
class WeatherCleanupServiceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private WeatherCleanupService weatherCleanupService;

    @Test
    @DisplayName("과거 예보 중 피드에서 사용되지 않는 Weather만 삭제한다")
    void deleteUnusedWeathersBeforeToday_removesOnlyUnusedPastWeathers() {
        ZoneId zone = DateTimeUtils.SEOUL_ZONE_ID;
        Instant todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant();

        Location location = Location.builder()
            .latitude(BigDecimal.valueOf(37.5))
            .longitude(BigDecimal.valueOf(127.0))
            .locationNames("서울")
            .build();
        entityManager.persist(location);

        Weather usedPast = Weather.builder()
            .location(location)
            .forecastAt(todayStart.minusSeconds(7_200))
            .forecastedAt(todayStart.minusSeconds(10_800))
            .build();

        Weather unusedPast = Weather.builder()
            .location(location)
            .forecastAt(todayStart.minusSeconds(3_600))
            .forecastedAt(todayStart.minusSeconds(7_200))
            .build();

        Weather futureWeather = Weather.builder()
            .location(location)
            .forecastAt(todayStart.plusSeconds(3_600))
            .forecastedAt(todayStart)
            .build();

        entityManager.persist(usedPast);
        entityManager.persist(unusedPast);
        entityManager.persist(futureWeather);
        entityManager.flush();

        Feed feed = Feed.of(UUID.randomUUID(), usedPast.getId(), "내용");
        entityManager.persist(feed);
        entityManager.flush();

        int deletedCount = weatherCleanupService.deleteUnusedWeathersBeforeToday();
        entityManager.clear();

        assertThat(deletedCount).isEqualTo(1);
        assertThat(weatherRepository.findById(unusedPast.getId())).isEmpty();
        assertThat(weatherRepository.findById(usedPast.getId())).isPresent();
        assertThat(weatherRepository.findById(futureWeather.getId())).isPresent();
    }
}
