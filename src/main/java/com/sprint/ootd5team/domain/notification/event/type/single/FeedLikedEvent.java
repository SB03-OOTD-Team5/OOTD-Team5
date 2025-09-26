package com.sprint.ootd5team.domain.notification.event.type.single;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.SingleReceiverEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.FeedLikedEvent.FeedLikedPayload;
import java.util.UUID;

/**
 * FEED_LIKED("%s님이 내 피드를 좋아합니다.", "%s"),
 * 작성자 이름, 피드 내용
 * <p>
 * receiverId =  피드 소유자(작성자) id
 */
@JsonTypeName("feed-liked")
public class FeedLikedEvent extends SingleReceiverEvent<FeedLikedPayload> {

    public FeedLikedEvent(UUID feedId, UUID ownerId, String feedContent, String likerName) {
        super(new FeedLikedPayload(feedId, feedContent, likerName), ownerId);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FEED_LIKED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().likerName(), getData().feedContent()};
    }

    public record FeedLikedPayload(UUID feedId, String feedContent, String likerName) {

    }
}
