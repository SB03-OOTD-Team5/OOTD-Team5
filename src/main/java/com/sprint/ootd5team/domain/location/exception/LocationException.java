package com.sprint.ootd5team.domain.location.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class LocationException extends OotdException {

    public LocationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public LocationException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public LocationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public void addDetail(String key, Object value) {
        this.getDetails().put(key, value);
    }
}
