package com.sprint.ootd5team.base.exception.clothesattribute;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class AttributeException extends OotdException {

    public AttributeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AttributeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

}
