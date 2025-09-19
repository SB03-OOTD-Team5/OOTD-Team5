package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class WeatherException extends OotdException {

    public WeatherException(ErrorCode errorCode) {
        super(errorCode);
    }

    public WeatherException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public WeatherException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
