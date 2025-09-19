package com.sprint.ootd5team.domain.profile.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class ProfileNotFoundException extends ProfileException {

    public ProfileNotFoundException() {
        super(ErrorCode.PROFILE_NOT_FOUND);
    }

    public ProfileNotFoundException(String customMessage) {
        super(ErrorCode.PROFILE_NOT_FOUND, customMessage);
    }
}
