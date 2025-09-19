package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class WeatherNotFoundException extends WeatherException {

    public WeatherNotFoundException() {
        super(ErrorCode.WEATHER_NOT_FOUND);
    }

    public WeatherNotFoundException(String customMessage) {
        super(ErrorCode.WEATHER_NOT_FOUND, customMessage);
    }
}
