package com.sprint.ootd5team.base.exception.jsoup;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;

public class JsoupException extends OotdException {

    public JsoupException(ErrorCode errorCode) {
        super(errorCode);
    }

    public JsoupException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
