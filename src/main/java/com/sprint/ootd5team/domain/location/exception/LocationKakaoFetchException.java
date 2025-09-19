package com.sprint.ootd5team.domain.location.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class LocationKakaoFetchException extends LocationException {

    public LocationKakaoFetchException() {
        super(ErrorCode.LOCATION_FETCH_FAILED);
    }

    public LocationKakaoFetchException(String customMessage) {
        super(ErrorCode.LOCATION_FETCH_FAILED, customMessage);
    }


}
