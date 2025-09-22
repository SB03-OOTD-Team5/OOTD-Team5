package com.sprint.ootd5team.base.sse;

import java.util.List;
import java.util.UUID;

public interface SseMessageRepository {

    void save(SseMessage message);

    List<SseMessage> findAfter(UUID lastEventId);
}
