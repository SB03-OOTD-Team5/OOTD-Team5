package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

/**
 * CLOTHES_ATTRIBUTE_ADDED("새로운 의상 속성이 추가되었어요.", "내 의상에 [%s]속성을 추가해보세요."),
 * 속성 이름
 *
 * receiverId = 모든 유저 id
 */
public class ClothesAttributeCreateEvent extends DomainEvent<ClothesAttributeDefDto> {

    public ClothesAttributeCreateEvent(ClothesAttributeDefDto data) {
        super(data);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.CLOTHES_ATTRIBUTE_CREATED;
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
