package com.sprint.ootd5team.base.sse.repository.message;

import com.sprint.ootd5team.base.sse.SseMessage;
import java.util.List;
import java.util.UUID;

public interface SseMessageRepository {

    void save(SseMessage message);

    List<SseMessage> findAfter(UUID userId, UUID lastEventId);
}
