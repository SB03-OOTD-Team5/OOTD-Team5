package com.sprint.ootd5team.base.exception.feed;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class FeedNotFoundException extends FeedException {

    public FeedNotFoundException() {
        super(ErrorCode.FEED_NOT_FOUND);
    }

    public static FeedNotFoundException withId (UUID feedId) {
        FeedNotFoundException exception = new FeedNotFoundException();
        exception.addDetail("feedId", feedId);
        return exception;
    }
}