package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

/**
 * CLOTHES_ATTRIBUTE_UPDATE("의상 속성이 변경되었어요.", "[%s]속성을 확인해보세요.");
 * 속성 이름
 *
 * receiverId = 모든 유저 id
 */
public class ClothesAttributeUpdateEvent extends DomainEvent<ClothesAttributeDefDto> {

    public ClothesAttributeUpdateEvent(ClothesAttributeDefDto data) {
        super(data);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.CLOTHES_ATTRIBUTE_UPDATED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().name()};
    }

    @Override
    public List<UUID> getReceiverIds() {
        return List.of();
    }
}
