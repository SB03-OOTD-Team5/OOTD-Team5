package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

public class ClothesAttributeCreateEvent extends DomainEvent<ClothesAttributeDefDto> {

    public ClothesAttributeCreateEvent(ClothesAttributeDefDto data) {
        super(data);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.CLOTHES_ATTRIBUTE_ADDED;
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
