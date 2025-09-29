package com.sprint.ootd5team.base.exception.directmessage;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class DirectMessageRoomCreationFailedException extends DirectMessageException {

    private DirectMessageRoomCreationFailedException(Throwable cause) {
        super(ErrorCode.DIRECT_MESSAGE_ROOM_CREATION_FAILED, cause);
    }

    public static DirectMessageRoomCreationFailedException withDmKey(
        String dmKey,
        UUID senderId,
        UUID receiverId,
        Throwable cause
    ) {
        DirectMessageRoomCreationFailedException exception = new DirectMessageRoomCreationFailedException(cause);
        exception.addDetail("dmKey", dmKey);
        if (senderId != null) {
            exception.addDetail("senderId", senderId);
        }
        if (receiverId != null) {
            exception.addDetail("receiverId", receiverId);
        }
        return exception;
    }
}