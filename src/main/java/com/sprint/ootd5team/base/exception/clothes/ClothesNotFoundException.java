package com.sprint.ootd5team.base.exception.clothes;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class ClothesNotFoundException extends ClothesException {

    public ClothesNotFoundException() {
        super(ErrorCode.CLOTHES_NOT_FOUND);
    }

    public static ClothesNotFoundException withId(UUID clothesId) {
        ClothesNotFoundException exception = new ClothesNotFoundException();
        exception.addDetail("clothesId", clothesId);
        return exception;
    }
}
