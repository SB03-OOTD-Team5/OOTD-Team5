package com.sprint.ootd5team.base.exception.clothesattribute;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class AttributeNotFoundException extends AttributeException {

    public AttributeNotFoundException() {
        super(ErrorCode.ATTRIBUTE_NOT_FOUND);
    }

    public static AttributeNotFoundException withId(UUID attributeId) {
        AttributeNotFoundException exception = new AttributeNotFoundException();
        exception.addDetail("attributeId", attributeId);
        return exception;
    }
}
