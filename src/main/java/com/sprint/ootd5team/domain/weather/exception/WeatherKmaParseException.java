package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class WeatherKmaParseException extends WeatherException {

    public WeatherKmaParseException() {
        super(ErrorCode.WEATHER_KMA_PARSE_FAILED);
    }

    public WeatherKmaParseException(String customMessage) {
        super(ErrorCode.WEATHER_KMA_PARSE_FAILED, customMessage);
    }
}
