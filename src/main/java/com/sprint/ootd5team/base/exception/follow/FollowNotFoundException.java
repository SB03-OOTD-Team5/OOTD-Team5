package com.sprint.ootd5team.base.exception.follow;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class FollowNotFoundException extends FollowException {

    public FollowNotFoundException() {
        super(ErrorCode.FOLLOW_NOT_FOUND);
    }

    public static FollowNotFoundException withId(UUID followId) {
        FollowNotFoundException exception = new FollowNotFoundException();
        exception.addDetail("followId", followId);
        return exception;
    }
}
