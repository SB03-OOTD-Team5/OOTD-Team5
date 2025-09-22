package com.sprint.ootd5team.base.exception.feed;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class LikeNotFoundException extends FeedException {

    public LikeNotFoundException() {
        super(ErrorCode.LIKE_NOT_FOUND_EXCEPTION);
    }

    public static LikeNotFoundException withIds (UUID feedId, UUID userId) {
        LikeNotFoundException exception = new LikeNotFoundException();
        exception.addDetail("feedId", feedId);
        exception.addDetail("userId", userId);
        return exception;
    }
}
