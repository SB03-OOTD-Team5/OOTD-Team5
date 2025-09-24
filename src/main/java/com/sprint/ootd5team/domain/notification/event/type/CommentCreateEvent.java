package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

//    eventPublisher.publishEvent(new CommentCreateEvent(dto, receiverId));
public class CommentCreateEvent extends DomainEvent<CommentDto> {
    private final UUID receiverId;

    public CommentCreateEvent(CommentDto data, UUID receiverId) {
        super(data);
        this.receiverId = receiverId;
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FEED_COMMENTED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().author().name(), getData().content()};
    }

    @Override
    public List<UUID> getReceiverIds() {
        return List.of(receiverId);
    }
}