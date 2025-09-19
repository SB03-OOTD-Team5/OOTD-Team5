package com.sprint.ootd5team.base.exception.feed;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class FeedException extends OotdException {

    public FeedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FeedException(ErrorCode errorCode, Throwable cause) { super(errorCode, cause); }
}
