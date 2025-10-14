package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class WeatherOpenParseException extends WeatherException {

    public WeatherOpenParseException() {
        super(ErrorCode.WEATHER_OPEN_PARSE_FAILED);
    }

    public WeatherOpenParseException(String customMessage) {
        super(ErrorCode.WEATHER_OPEN_PARSE_FAILED, customMessage);
    }
}
