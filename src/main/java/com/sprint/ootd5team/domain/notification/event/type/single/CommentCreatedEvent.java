package com.sprint.ootd5team.domain.notification.event.type.single;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.SingleReceiverEvent;
import java.util.UUID;

/**
 * FEED_COMMENTED("%s님이 댓글을 달았어요.", "%s"),
 * 댓글 작성자 이름, 댓글 내용
 * <p>
 * receiverId = 댓글이 달린 피드의 생성자 id
 * eventPublisher.publishEvent(new CommentCreatedEvent(dto, receiverId));
 */
@JsonTypeName("comment-created")
public class CommentCreatedEvent extends SingleReceiverEvent<CommentDto> {

    @JsonCreator
    public CommentCreatedEvent(
        @JsonProperty("data") CommentDto data,
        @JsonProperty("receiver") UUID receiverId
    ) {
        super(data, receiverId);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FEED_COMMENTED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().author().name(), getData().content()};
    }

}