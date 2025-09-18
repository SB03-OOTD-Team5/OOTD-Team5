package com.sprint.ootd5team.base.exception.profile;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class ProfileNotFoundException extends ProfileException {

    public ProfileNotFoundException() {
        super(ErrorCode.PROFILE_NOT_FOUND);
    }

    public static ProfileNotFoundException withId (UUID profileId) {
        ProfileNotFoundException exception = new ProfileNotFoundException();
        exception.addDetail("profileId", profileId);
        return exception;
    }

    public static ProfileNotFoundException withUserId (UUID userId) {
        ProfileNotFoundException exception = new ProfileNotFoundException();
        exception.addDetail("userId", userId);
        return exception;
    }
}
