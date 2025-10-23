package com.sprint.ootd5team.domain.notification.event.type.multi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.clothesattribute.dto.ClothesAttributeDefDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.MultiReceiverEvent;
import java.util.List;
import java.util.UUID;

/**
 * CLOTHES_ATTRIBUTE_ADDED("새로운 의상 속성이 추가되었어요.", "내 의상에 [%s]속성을 추가해보세요."),
 * 속성 이름
 * <p>
 * receiverId = 모든 유저 id
 */
@JsonTypeName("attribute-created")
public class ClothesAttributeCreatedEvent extends MultiReceiverEvent<ClothesAttributeDefDto> {

    @JsonCreator
    public ClothesAttributeCreatedEvent(
        @JsonProperty("data") ClothesAttributeDefDto data,
        @JsonProperty("receivers") List<UUID> receivers
    ) {
        super(data, receivers);
    }

    public ClothesAttributeCreatedEvent(ClothesAttributeDefDto data) {
        super(data, List.of());
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.CLOTHES_ATTRIBUTE_CREATED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().name()};
    }

}
