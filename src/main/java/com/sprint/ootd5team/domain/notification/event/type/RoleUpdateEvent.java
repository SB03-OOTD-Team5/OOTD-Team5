package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

/**
 * ROLE_CHANGED("내 권한이 변경되었어요.", "내 권한이 [%s]에서 [%s]로 변경 되었어요."),
 * 기존 권한, 새 권한
 *
 * receiverId = 권한이 변경된 유저 id
 */
public class RoleUpdateEvent extends DomainEvent<Void> {
    private final UUID userId;
    private final String oldRole;
    private final String newRole;

    public RoleUpdateEvent(UUID userId, String oldRole, String newRole) {
        super(null);
        this.userId = userId;
        this.oldRole = oldRole;
        this.newRole = newRole;
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.ROLE_UPDATED;
    }

    @Override
    public List<UUID> getReceiverIds() {
        return List.of(userId);
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{oldRole, newRole};
    }
}