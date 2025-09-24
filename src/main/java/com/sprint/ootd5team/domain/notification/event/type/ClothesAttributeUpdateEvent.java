package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

public class ClothesAttributeUpdateEvent extends DomainEvent<ClothesAttributeDefDto> {

    public ClothesAttributeUpdateEvent(ClothesAttributeDefDto data) {
        super(data);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.CLOTHES_ATTRIBUTE_UPDATE;
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
