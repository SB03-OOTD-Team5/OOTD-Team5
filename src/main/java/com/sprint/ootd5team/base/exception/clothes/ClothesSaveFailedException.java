package com.sprint.ootd5team.base.exception.clothes;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class ClothesSaveFailedException extends ClothesException {

    public ClothesSaveFailedException() {
        super(ErrorCode.CLOTHES_SAVE_FAILED);
    }

    public static ClothesSaveFailedException withId(UUID clothesId) {
        ClothesSaveFailedException ex = new ClothesSaveFailedException();
        ex.addDetail("clothesId", clothesId);
        return ex;
    }
}
