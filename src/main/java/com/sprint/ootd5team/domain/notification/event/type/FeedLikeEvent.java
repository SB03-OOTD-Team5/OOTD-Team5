package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

/**
 * FEED_LIKED("%s님이 내 피드를 좋아합니다.", "%s"),
 * 작성자 이름, 피드 내용
 *
 * receiverId = 피드에 좋아요를 누른 유저 id
 */
public class FeedLikeEvent extends DomainEvent<Void> {
    private final UUID feedId;
    private final UUID ownerId;
    private final String feedContent;
    private final String likerName;

    public FeedLikeEvent(UUID feedId, UUID ownerId, String feedContent, String likerName) {
        super(null);
        this.feedId = feedId;
        this.ownerId = ownerId;
        this.feedContent = feedContent;
        this.likerName = likerName;
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FEED_LIKED;
    }

    @Override
    public List<UUID> getReceiverIds() {
        return List.of(ownerId);
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{likerName, feedContent};
    }
}
