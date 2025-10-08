package com.sprint.ootd5team.domain.weather.external.meteo;

import com.sprint.ootd5team.domain.location.entity.Location;
import com.sprint.ootd5team.domain.weather.entity.Weather;
import com.sprint.ootd5team.domain.weather.external.WeatherFactory;
import com.sprint.ootd5team.domain.weather.external.context.ForecastIssueContext;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class OpenMeteoFactory implements WeatherFactory<ForecastIssueContext, OpenMeteoResponse> {

    @Override
    public List<Weather> findOrCreateWeathers(BigDecimal latitude, BigDecimal longitude) {
        return List.of();
    }

    @Override
    public List<Weather> findWeathers(Location location, ForecastIssueContext context) {
        return List.of();
    }

    @Override
    public List<Weather> createWeathers(OpenMeteoResponse response, List<Weather> existingWeathers,
        ForecastIssueContext context, Location location) {
        return List.of();
    }

    @Override
    public ForecastIssueContext createForecastIssueContext(ZonedDateTime reference) {
        return null;
    }
}
