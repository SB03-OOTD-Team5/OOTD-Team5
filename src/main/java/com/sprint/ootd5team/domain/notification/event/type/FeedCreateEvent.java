package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

/**
 * FEED_FOLLOW_CREATED("%s님이 새로운 피드를 작성했어요.", "%s"),
 * 작성자 이름, 피드 내용
 *
 * receiverId = 피드 작성자를 팔로우하고 있는 유저 id
 */
public class FeedCreateEvent extends DomainEvent<FeedDto> {
    private final List<UUID> receiverIds;

    public FeedCreateEvent(FeedDto data, List<UUID> receiverIds) {
        super(data);
        this.receiverIds = receiverIds;
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FEED_FOLLOW_CREATED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().author().name(), getData().content()};
    }

    @Override
    public List<UUID> getReceiverIds() {
        return receiverIds;     // 피드 작성자를 팔로우하고 있는 사용자 id
    }
}
