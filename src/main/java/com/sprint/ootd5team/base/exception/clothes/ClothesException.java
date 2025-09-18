package com.sprint.ootd5team.base.exception.clothes;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class ClothesException extends OotdException {

    public ClothesException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ClothesException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

}
