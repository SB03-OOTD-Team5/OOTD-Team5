package com.sprint.ootd5team.base.sse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SseMessage {

    private final UUID id;
    private final String eventName;
    private final Object data;
    private Set<UUID> targetUserIds;

    // Jackson 역직렬화용 생성자
    @Builder
    @JsonCreator
    public SseMessage(
        @JsonProperty("id") UUID id,
        @JsonProperty("eventName") String eventName,
        @JsonProperty("data") Object data,
        @JsonProperty("targetUserIds") Set<UUID> targetUserIds
    ) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.eventName = eventName;
        this.data = data;
        this.targetUserIds = targetUserIds;
    }

    public SseMessage(String eventName, Object data) {
        this(null, eventName, data, null);

    }

    public SseMessage(UUID id, String eventName, Object data) {
        this(id, eventName, data, null);

    }
}
