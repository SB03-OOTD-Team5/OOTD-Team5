package com.sprint.ootd5team.domain.notification.event.type.single;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.SingleReceiverEvent;

/**
 * DM_RECEIVED("[DM] %s", "%s"),
 * 메시지 발신자(sender) 이름, 메시지 내용
 * <p>
 * receiverId = 수신자 id
 */
@JsonTypeName("dm-created")
public class DmCreatedEvent extends SingleReceiverEvent<DirectMessageDto> {

    public DmCreatedEvent(DirectMessageDto data) {
        super(data, data.receiver().userId());
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.DM_RECEIVED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().sender().name(), getData().content()};
    }

}
