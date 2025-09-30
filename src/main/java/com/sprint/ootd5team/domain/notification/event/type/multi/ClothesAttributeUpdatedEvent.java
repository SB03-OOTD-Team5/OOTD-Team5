package com.sprint.ootd5team.domain.notification.event.type.multi;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.MultiReceiverEvent;
import java.util.List;

/**
 * CLOTHES_ATTRIBUTE_UPDATE("의상 속성이 변경되었어요.", "[%s]속성을 확인해보세요.");
 * 속성 이름
 * <p>
 * receiverId = 모든 유저 id
 */
@JsonTypeName("attribute-updated")
public class ClothesAttributeUpdatedEvent extends MultiReceiverEvent<ClothesAttributeDefDto> {

    public ClothesAttributeUpdatedEvent(ClothesAttributeDefDto data) {
        super(data, List.of());
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.CLOTHES_ATTRIBUTE_UPDATED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().name()};
    }

}
