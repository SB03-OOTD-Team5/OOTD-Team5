package com.sprint.ootd5team.domain.notification.event.type.single;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.SingleReceiverEvent;
import com.sprint.ootd5team.domain.notification.event.type.single.RoleUpdatedEvent.RoleUpdatedPayload;
import java.util.UUID;

/**
 * ROLE_UPDATED("내 권한이 변경되었어요.", "내 권한이 [%s]에서 [%s]로 변경 되었어요."),
 * 기존 권한, 새 권한
 * <p>
 * receiverId = 권한이 변경된 유저 id
 */
@JsonTypeName("role-updated")
public class RoleUpdatedEvent extends SingleReceiverEvent<RoleUpdatedPayload> {

    @JsonCreator
    public RoleUpdatedEvent(
        @JsonProperty("data") RoleUpdatedPayload data,
        @JsonProperty("receiver") UUID receiver
    ) {
        super(data, receiver);
    }

    public RoleUpdatedEvent(UUID receiverId, String oldRole, String newRole) {
        super(new RoleUpdatedPayload(oldRole, newRole), receiverId);
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.ROLE_UPDATED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().oldRole(), getData().newRole()};
    }

    public record RoleUpdatedPayload(String oldRole, String newRole) {

    }
}