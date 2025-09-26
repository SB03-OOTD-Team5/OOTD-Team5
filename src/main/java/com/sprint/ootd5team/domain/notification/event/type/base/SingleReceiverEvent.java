package com.sprint.ootd5team.domain.notification.event.type.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public abstract class SingleReceiverEvent<T> extends DomainEvent<T> {

    @JsonProperty("receiver")
    private final UUID receiver;

    protected SingleReceiverEvent(
        @JsonProperty("data") T data,
        @JsonProperty("data") UUID receiver
    ) {
        super(data);
        this.receiver = receiver;
    }

    @Override
    public List<UUID> getReceiverIds() {
        return List.of(receiver);
    }
}