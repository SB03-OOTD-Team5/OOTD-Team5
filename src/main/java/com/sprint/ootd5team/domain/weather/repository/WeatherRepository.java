package com.sprint.ootd5team.domain.weather.repository;

import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {

    List<Weather> findAllByForecastedAtAndLatitudeAndLongitude(
        Instant forecastedAt, BigDecimal latitude, BigDecimal longitude);

    Optional<Weather> findFirstByLatitudeAndLongitudeAndForecastAtBetweenOrderByForecastAtDesc(
        BigDecimal latitude, BigDecimal longitude, Instant startOfDay, Instant
            endOfDay);

    List<Weather> findAllByPrecipitationTypeEqualsOrSkyStatusEquals(
        PrecipitationType precipitationType, SkyStatus skyStatus);

}
