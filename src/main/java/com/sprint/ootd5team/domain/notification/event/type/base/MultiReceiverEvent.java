package com.sprint.ootd5team.domain.notification.event.type.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public abstract class MultiReceiverEvent<T> extends DomainEvent<T> {

    @JsonProperty("receivers")
    private final List<UUID> receivers;

    protected MultiReceiverEvent(
        @JsonProperty("data") T data,
        @JsonProperty("receivers") List<UUID> receivers
    ) {
        super(data);
        this.receivers = receivers;
    }

    @Override
    public List<UUID> getReceiverIds() {
        return receivers;
    }
}