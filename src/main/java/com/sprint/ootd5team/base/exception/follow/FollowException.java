package com.sprint.ootd5team.base.exception.follow;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class FollowException extends OotdException {

    public FollowException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FollowException(ErrorCode errorCode, Throwable cause) { super(errorCode, cause); }
}
