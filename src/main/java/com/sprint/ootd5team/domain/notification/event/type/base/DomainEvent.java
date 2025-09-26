package com.sprint.ootd5team.domain.notification.event.type.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.multi.ClothesAttributeCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.multi.ClothesAttributeUpdatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.multi.FeedCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.CommentCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.DmCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.FeedLikedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.FollowCreatedEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.RoleUpdatedEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ClothesAttributeCreatedEvent.class, name = "attribute-created"),
    @JsonSubTypes.Type(value = ClothesAttributeUpdatedEvent.class, name = "attribute-updated"),
    @JsonSubTypes.Type(value = CommentCreatedEvent.class, name = "comment-created"),
    @JsonSubTypes.Type(value = DmCreatedEvent.class, name = "dm-created"),
    @JsonSubTypes.Type(value = FeedCreatedEvent.class, name = "feed-created"),
    @JsonSubTypes.Type(value = FeedLikedEvent.class, name = "feed-liked"),
    @JsonSubTypes.Type(value = FollowCreatedEvent.class, name = "follow-created"),
    @JsonSubTypes.Type(value = RoleUpdatedEvent.class, name = "role-updated"),
})
public abstract class DomainEvent<T> {

    // 수신자 리스트 (비어있으면 전체 전송)
    private final Instant createdAt;
    private T data;

    protected DomainEvent(T data) {
        this.data = data;
        this.createdAt = Instant.now();
    }

    // 각 이벤트별로 어떤 알림으로 변환되는지 정의
    public abstract NotificationTemplateType getTemplateType();

    // 기본 레벨은 INFO
    public NotificationLevel getLevel() {
        return NotificationLevel.INFO;
    }

    @JsonIgnore
    public abstract List<UUID> getReceiverIds();

    // 템플릿 args (예: "%s님이 좋아합니다." → 사용자 이름)
    public abstract Object[] getArgs();
}
