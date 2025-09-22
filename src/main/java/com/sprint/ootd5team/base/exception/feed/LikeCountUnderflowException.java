package com.sprint.ootd5team.base.exception.feed;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class LikeCountUnderflowException extends FeedException {

    public LikeCountUnderflowException() {
        super(ErrorCode.LIKE_COUNT_UNDER_FLOW_EXCEPTION);
    }

    public static LikeCountUnderflowException withFeedId(UUID feedId) {
        LikeCountUnderflowException exception = new LikeCountUnderflowException();
        exception.addDetail("feedId", feedId);
        return exception;
    }
}
