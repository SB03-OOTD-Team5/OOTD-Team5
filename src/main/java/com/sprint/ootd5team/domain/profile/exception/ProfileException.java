package com.sprint.ootd5team.domain.profile.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class ProfileException extends OotdException {

    public ProfileException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProfileException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

    public ProfileException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
