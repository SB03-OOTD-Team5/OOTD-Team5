package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

//    eventPublisher.publishEvent(new FeedCreatedEvent(dto, receiverId));
public class FeedCreatedEvent extends DomainEvent<FeedDto> {
    private final UUID receiverId;

    public FeedCreatedEvent(FeedDto data, UUID receiverId) {
        super(data);
        this.receiverId = receiverId;
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FEED_FOLLOW_CREATED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().content()};   // 좋아요 누른 사람 이름도 필요
    }

    @Override
    public List<UUID> getReceiverIds() {
        return List.of(receiverId);   // 피드 작성자를 팔로우하고 있는 사용자 id
        // 이벤트 발행시 조회할건지
    }
}
