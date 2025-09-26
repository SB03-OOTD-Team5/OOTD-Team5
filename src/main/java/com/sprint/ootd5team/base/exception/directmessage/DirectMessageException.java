package com.sprint.ootd5team.base.exception.directmessage;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class DirectMessageException extends OotdException {

    public DirectMessageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DirectMessageException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}