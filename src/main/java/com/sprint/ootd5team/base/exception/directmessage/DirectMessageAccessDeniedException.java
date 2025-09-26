package com.sprint.ootd5team.base.exception.directmessage;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class DirectMessageAccessDeniedException extends DirectMessageException {

    private DirectMessageAccessDeniedException() {
        super(ErrorCode.DIRECT_MESSAGE_ACCESS_DENIED);
    }

    public static DirectMessageAccessDeniedException notParticipant(UUID roomId, UUID userId) {
        DirectMessageAccessDeniedException exception = new DirectMessageAccessDeniedException();
        exception.addDetail("reason", "NOT_ROOM_PARTICIPANT");
        if (roomId != null) {
            exception.addDetail("roomId", roomId);
        }
        if (userId != null) {
            exception.addDetail("userId", userId);
        }
        return exception;
    }

    public static DirectMessageAccessDeniedException forPartner(UUID partnerUserId, UUID requesterId) {
        DirectMessageAccessDeniedException exception = new DirectMessageAccessDeniedException();
        exception.addDetail("reason", "INVALID_DM_PARTNER");
        if (partnerUserId != null) {
            exception.addDetail("partnerUserId", partnerUserId);
        }
        if (requesterId != null) {
            exception.addDetail("requesterId", requesterId);
        }
        return exception;
    }
}