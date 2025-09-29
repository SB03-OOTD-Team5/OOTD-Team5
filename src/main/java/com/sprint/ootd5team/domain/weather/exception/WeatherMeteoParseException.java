package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class WeatherMeteoParseException extends WeatherException {

    public WeatherMeteoParseException() {
        super(ErrorCode.WEATHER_METEO_PARSE_FAILED);
    }

    public WeatherMeteoParseException(String customMessage) {
        super(ErrorCode.WEATHER_METEO_PARSE_FAILED, customMessage);
    }
}

