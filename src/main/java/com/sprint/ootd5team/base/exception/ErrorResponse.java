package com.sprint.ootd5team.base.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

    private final String message;
    private final Map<String, Object> details;
    private final String exceptionName;


    public ErrorResponse(OotdException exception) {
        this(exception.getMessage(), exception.getDetails(), exception.getErrorCode().name());
    }

    public ErrorResponse(Exception exception) {
        this(exception.getMessage(), new HashMap<>(), exception.getClass().getSimpleName());
    }
}