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
}