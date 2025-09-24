package com.sprint.ootd5team.domain.notification.event.type;

import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import java.util.List;
import java.util.UUID;

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
        return NotificationTemplateType.ROLE_CHANGED;
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