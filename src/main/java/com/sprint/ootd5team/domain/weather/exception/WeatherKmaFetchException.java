package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class WeatherKmaFetchException extends WeatherException {

    public WeatherKmaFetchException() {
        super(ErrorCode.WEATHER_KMA_FETCH_FAILED);
    }

    public WeatherKmaFetchException(String customMessage) {
        super(ErrorCode.WEATHER_KMA_FETCH_FAILED, customMessage);
    }
}
