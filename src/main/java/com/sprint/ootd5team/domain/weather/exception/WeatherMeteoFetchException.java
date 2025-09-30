package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class WeatherMeteoFetchException extends WeatherException {

    public WeatherMeteoFetchException() {
        super(ErrorCode.WEATHER_METEO_FETCH_FAILED);
    }

    public WeatherMeteoFetchException(String customMessage) {
        super(ErrorCode.WEATHER_METEO_FETCH_FAILED, customMessage);
    }
}

