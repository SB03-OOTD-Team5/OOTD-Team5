package com.sprint.ootd5team.domain.weather.repository;

import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {


    Optional<Weather> findFirstByLocationIdAndForecastAtBetweenOrderByForecastAtDesc(
        UUID locationId, Instant startOfDay, Instant
            endOfDay);

    List<Weather> findAllByPrecipitationTypeEqualsOrSkyStatusEquals(
        PrecipitationType precipitationType, SkyStatus skyStatus);

    List<Weather> findAllByLocationIdAndForecastedAt(UUID locationId, Instant forecastedAt);

    // 최신 1건
    Weather findTopByLocationIdOrderByForecastedAtDescForecastAtDescCreatedAtDesc(UUID locationId);

    boolean existsByLocationIdAndForecastedAt(UUID locationId, Instant forecastedAt);

}
