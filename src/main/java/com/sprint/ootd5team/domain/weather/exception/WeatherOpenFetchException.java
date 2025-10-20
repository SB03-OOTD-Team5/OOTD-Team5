package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class WeatherOpenFetchException extends WeatherException {

    public WeatherOpenFetchException() {
        super(ErrorCode.WEATHER_OPEN_FETCH_FAILED);
    }

    public WeatherOpenFetchException(String customMessage) {
        super(ErrorCode.WEATHER_OPEN_FETCH_FAILED, customMessage);
    }
}
