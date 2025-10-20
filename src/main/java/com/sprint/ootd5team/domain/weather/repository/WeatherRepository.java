package com.sprint.ootd5team.domain.weather.repository;

import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    List<Weather> findAllByLocationIdAndForecastedAtAndForecastAt(UUID locationId,
        Instant forecastedAt, Instant forecastAt);


    List<Weather> findAllByLocationIdAndForecastedAtAndForecastAtIn(UUID locationId,
        Instant forecastedAt, List<Instant> forecastAts);


    List<Weather> findAllByLocationIdAndForecastedAtAndForecastAtBetween(UUID locationId,
        Instant forecastedAt, Instant startInclusive, Instant endExclusive);


    List<Weather> findFirstByLocationIdAndForecastAtOrderByForecastedAtDesc(UUID locationId,
        Instant forecastAt);

    long deleteByLocationIdAndForecastAt(UUID locationId, Instant forecastAt);

    Optional<Weather> findFirstByLocationIdOrderByForecastedAtDesc(UUID locationId);


    Optional<Weather> findFirstByLocationIdAndForecastAtBetweenOrderByForecastedAtDescForecastAtDescCreatedAtDesc(
        UUID locationId, Instant startOfDay, Instant endOfDay);

    boolean existsByLocationIdAndForecastedAt(UUID locationId, Instant forecastedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from Weather w
        where w.forecastAt < :beforeInstant
          and not exists (
            select 1 from Feed f
            where f.weatherId = w.id
        )
        """)
    int deleteUnusedBefore(@Param("beforeInstant") Instant beforeInstant);

}
