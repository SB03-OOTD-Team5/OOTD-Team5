package com.sprint.ootd5team.domain.weather.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class ConvertCoordFailException extends WeatherException {

    public ConvertCoordFailException() {
        super(ErrorCode.COORD_CONVERT_FAILED);
    }

    public ConvertCoordFailException(String customMessage) {
        super(ErrorCode.COORD_CONVERT_FAILED, customMessage);
    }
}
