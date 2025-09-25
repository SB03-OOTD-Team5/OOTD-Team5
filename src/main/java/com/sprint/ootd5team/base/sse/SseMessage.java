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
@Builder
public class SseMessage {

    @Builder.Default
    private final UUID id = UUID.randomUUID();

    private final String eventName;
    private final Object data;
    private Set<UUID> targetUserIds;

    public SseMessage(String eventName, Object data) {
        this.id = UUID.randomUUID();
        this.eventName = eventName;
        this.data = data;
    }

    // Jackson 역직렬화용 생성자
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

    // 테스트용 편의 메서드
    public SseMessage(UUID id, String eventName, Object data) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.eventName = eventName;
        this.data = data;
        this.targetUserIds = null;
    }
}
