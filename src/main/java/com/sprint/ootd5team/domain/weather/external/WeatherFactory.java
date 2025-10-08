package com.sprint.ootd5team.domain.weather.external;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface WeatherFactory<C, R> {

    List<Weather> findOrCreateWeathers(BigDecimal latitude, BigDecimal longitude);

    List<Weather> findWeathers(Location location, C context);

    List<Weather> createWeathers(R response, List<Weather> existingWeathers, C context,
        Location location);

    ForecastIssueContext createForecastIssueContext(ZonedDateTime reference);
}
