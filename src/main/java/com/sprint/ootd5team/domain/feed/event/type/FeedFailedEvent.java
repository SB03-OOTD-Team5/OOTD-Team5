package com.sprint.ootd5team.domain.feed.event.type;

import java.time.Instant;

public record FeedFailedEvent(
    Instant timestamp,
    String topic,
    String eventType,
    Object event,
    String error,
    String stackTrace
) {}