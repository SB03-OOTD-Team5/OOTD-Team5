package com.sprint.ootd5team.base.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class OotdException extends RuntimeException {
    private final Instant timestamp;
    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public OotdException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }

    public OotdException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.timestamp = Instant.now();
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }

    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
}
