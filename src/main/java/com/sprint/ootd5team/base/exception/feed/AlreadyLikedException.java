package com.sprint.ootd5team.base.exception.feed;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class AlreadyLikedException extends FeedException {

    public AlreadyLikedException() {
        super(ErrorCode.ALREADY_LIKED_EXCEPTION);
    }

    public static AlreadyLikedException withIds(UUID feedId, UUID userId) {
        AlreadyLikedException exception = new AlreadyLikedException();
        exception.addDetail("feedId", feedId);
        exception.addDetail("userId", userId);
        return exception;
    }
}
