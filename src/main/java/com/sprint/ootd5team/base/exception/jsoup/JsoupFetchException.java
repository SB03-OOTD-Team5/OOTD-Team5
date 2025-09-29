package com.sprint.ootd5team.base.exception.jsoup;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class JsoupFetchException extends JsoupException {

    public JsoupFetchException(String url, Throwable cause) {
        super(ErrorCode.SCRAPING_FAILED, cause);
        addDetail("url", url);
    }
}