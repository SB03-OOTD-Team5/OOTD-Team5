package com.sprint.ootd5team.domain.feed.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class FeedNotFoundException extends RuntimeException {

    private final UUID feedId;

    public FeedNotFoundException(UUID feedId) {
        super("존재하지 않는 피드입니다. feedId: " + feedId);
        this.feedId = feedId;
    }
}