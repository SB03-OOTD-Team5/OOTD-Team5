package com.sprint.ootd5team.base.sse;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SseMessage {

    private final UUID id;
    private final String eventName;
    private final Object data;

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
        @JsonProperty("data") Object data
    ) {
        this.id = id;
        this.eventName = eventName;
        this.data = data;
    }
}
