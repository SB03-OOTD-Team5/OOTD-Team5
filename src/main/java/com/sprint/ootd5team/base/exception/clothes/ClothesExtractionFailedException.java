package com.sprint.ootd5team.base.exception.clothes;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class ClothesExtractionFailedException extends ClothesException {

    public ClothesExtractionFailedException() {
        super(ErrorCode.CLOTHES_EXTRACTION_FAILED);
    }

    public static ClothesExtractionFailedException withUrl(String targetUrl) {
        ClothesExtractionFailedException exception = new ClothesExtractionFailedException();
        exception.addDetail("targetUrl", targetUrl);
        return exception;
    }

    public static ClothesExtractionFailedException emptyResponse() {
        ClothesExtractionFailedException exception = new ClothesExtractionFailedException();
        exception.addDetail("reason", "LLM 응답이 비어 있음");
        return exception;
    }

    public static ClothesExtractionFailedException invalidJson() {
        ClothesExtractionFailedException exception = new ClothesExtractionFailedException();
        exception.addDetail("reason", "LLM 응답이 JSON 형식이 아님");
        return exception;
    }

    public static ClothesExtractionFailedException parsingError(Throwable cause) {
        ClothesExtractionFailedException exception = new ClothesExtractionFailedException();
        exception.addDetail("reason", "JSON 파싱 실패");
        exception.initCause(cause);
        return exception;
    }

    public static ClothesExtractionFailedException ollamaCallFailed(Throwable cause) {
        ClothesExtractionFailedException exception = new ClothesExtractionFailedException();
        exception.addDetail("reason", "Ollama LLM 호출 실패");
        exception.initCause(cause);
        return exception;
    }
}