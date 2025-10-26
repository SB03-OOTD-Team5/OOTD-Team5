package com.sprint.ootd5team.domain.notification.event.type.multi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.MultiReceiverEvent;
import com.sprint.ootd5team.domain.notification.event.type.multi.FeedCreatedEvent.FeedCreatedPayload;
import java.util.List;
import java.util.UUID;

/**
 * FEED_FOLLOW_CREATED("%s님이 새로운 피드를 작성했어요.", "%s"),
 * 작성자 이름, 피드 내용
 * <p>
 * receiverId = 피드 작성자를 팔로우하고 있는 유저 id
 */
@JsonTypeName("feed-created")
public class FeedCreatedEvent extends MultiReceiverEvent<FeedCreatedPayload> {

    @JsonCreator
    public FeedCreatedEvent(
        @JsonProperty("data") FeedCreatedPayload data,
        @JsonProperty("receivers") List<UUID> receivers
    ) {
        super(data, receivers);
    }

    public FeedCreatedEvent(UUID feedId, UUID authorId, String authorName, String content,
        List<UUID> receiverIds) {
        super(new FeedCreatedPayload(feedId, authorId, authorName, content),
            receiverIds == null ? List.of() : receiverIds);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FEED_FOLLOW_CREATED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{
            getData().authorName(), getData().content()
        };
    }

    public record FeedCreatedPayload(
        UUID feedId, UUID authorId, String authorName, String content) {

    }
}
