package com.sprint.ootd5team.base.exception.clothes;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class LlmFailedException extends ClothesException {

    public LlmFailedException() {
        super(ErrorCode.CLOTHES_EXTRACTION_FAILED);
    }

    public static LlmFailedException withUrl(String targetUrl) {
        LlmFailedException exception = new LlmFailedException();
        exception.addDetail("targetUrl", targetUrl);
        return exception;
    }

    public static LlmFailedException emptyResponse() {
        LlmFailedException exception = new LlmFailedException();
        exception.addDetail("reason", "LLM 응답이 비어 있음");
        return exception;
    }

    public static LlmFailedException invalidJson() {
        LlmFailedException exception = new LlmFailedException();
        exception.addDetail("reason", "LLM 응답이 JSON 형식이 아님");
        return exception;
    }

    public static LlmFailedException parsingError(Throwable cause) {
        LlmFailedException exception = new LlmFailedException();
        exception.addDetail("reason", "JSON 파싱 실패");
        exception.initCause(cause);
        return exception;
    }

    public static LlmFailedException geminiCallFailed(Throwable cause) {
        LlmFailedException exception = new LlmFailedException();
        exception.addDetail("reason", "Gemini LLM 호출 실패");
        exception.initCause(cause);
        return exception;
    }
}