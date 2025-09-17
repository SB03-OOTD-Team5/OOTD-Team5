package com.sprint.ootd5team.domain.feed.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;
import java.util.UUID;
import lombok.Getter;

@Getter
public class FeedNotFoundException extends OotdException {

    private final UUID feedId;

    public FeedNotFoundException(UUID feedId) {
        super(ErrorCode.FEED_NOT_FOUND);
        this.feedId = feedId;
        this.addDetail("feedId", feedId);
    }
}