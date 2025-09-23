package com.sprint.ootd5team.base.sse;

import java.util.Collection;
import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {

    SseEmitter connect(UUID userId, UUID lastEventId);

    void send(Collection<UUID> receiverIds, String eventName, Object data);

    void broadcast(String eventName, Object data);

    void cleanUp();

}
