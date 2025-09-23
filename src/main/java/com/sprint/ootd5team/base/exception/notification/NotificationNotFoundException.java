package com.sprint.ootd5team.base.exception.notification;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import java.util.Set;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    public static NotificationNotFoundException withId(UUID notificationId) {
        NotificationNotFoundException exception = new NotificationNotFoundException();
        exception.addDetail("notificationId", notificationId);
        return exception;
    }

    public static NotificationNotFoundException withIds(Set<UUID> notificationId) {
        NotificationNotFoundException exception = new NotificationNotFoundException();
        exception.addDetail("notificationId", notificationId);
        return exception;
    }
}
