package com.sprint.ootd5team.domain.notification.event.type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sprint.ootd5team.domain.notification.enums.NotificationLevel;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public abstract class DomainEvent<T> {
    private T data;
    private final Instant createdAt;

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

    // 알림 대상자 (receiverId) – 이벤트마다 달라질 수 있음
    public abstract List<UUID> getReceiverIds(); // 비어있으면 전체 전송

    // 템플릿 args (예: "%s님이 좋아합니다." → 사용자 이름)
    public abstract Object[] getArgs();
}
