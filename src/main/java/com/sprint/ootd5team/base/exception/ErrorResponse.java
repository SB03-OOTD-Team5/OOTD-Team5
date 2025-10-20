package com.sprint.ootd5team.base.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "API 오류 응답", example = "{\n  \"exceptionName\": \"string\",\n  \"message\": \"string\",\n  \"details\": {\n    \"additionalProp1\": \"string\",\n    \"additionalProp2\": \"string\",\n    \"additionalProp3\": \"string\"\n  }\n}")
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
