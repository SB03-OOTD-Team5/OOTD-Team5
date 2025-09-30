package com.sprint.ootd5team.domain.weather.repository;

import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {


    Optional<Weather> findFirstByLocationIdAndForecastAtBetweenOrderByForecastAtDesc(
        UUID locationId, Instant startOfDay, Instant endOfDay);

    List<Weather> findAllByPrecipitationTypeEqualsOrSkyStatusEquals(
        PrecipitationType precipitationType, SkyStatus skyStatus);

    List<Weather> findAllByLocationIdAndForecastedAt(UUID locationId, Instant forecastedAt);

    long deleteByLocationIdAndForecastAt(UUID locationId, Instant forecastAt);

    Optional<Weather> findFirstByLocationIdOrderByForecastedAtDesc(UUID locationId);


    @Query("""
        SELECT w
        FROM Weather w
        WHERE w.location.id = :locationId
          AND w.forecastAt >= :startOfDay
          AND w.forecastAt < :endOfDay
        ORDER BY w.forecastedAt DESC, w.forecastAt DESC, w.createdAt DESC
        """)
    Weather findTopByLocationIdAndForecastDateOrderByLatest(
        @Param("locationId") UUID locationId,
        @Param("startOfDay") Instant startOfDay,
        @Param("endOfDay") Instant endOfDay);

    boolean existsByLocationIdAndForecastedAt(UUID locationId, Instant forecastedAt);

}