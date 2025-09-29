package com.sprint.ootd5team.base.exception.follow;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class FollowAlreadyDeletedException extends FollowException {

    public FollowAlreadyDeletedException() { super(ErrorCode.FOLLOW_ALREADY_DELETED); }

    public static FollowAlreadyDeletedException withId(UUID followId) {
        FollowAlreadyDeletedException exception = new FollowAlreadyDeletedException();
        exception.addDetail("followId", followId);
        return exception;
    }
}
