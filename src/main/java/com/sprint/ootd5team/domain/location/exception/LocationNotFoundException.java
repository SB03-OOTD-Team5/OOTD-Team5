package com.sprint.ootd5team.domain.location.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class LocationNotFoundException extends LocationException {

    public LocationNotFoundException() {
        super(ErrorCode.LOCATION_NOT_FOUND);
    }

    public LocationNotFoundException(String customMessage) {
        super(ErrorCode.LOCATION_NOT_FOUND, customMessage);
    }
}
