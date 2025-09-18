package com.sprint.ootd5team.base.exception.clothesattribute;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class AttributeAlreadyExistException extends AttributeException {
    public AttributeAlreadyExistException() {
        super(ErrorCode.ATTRIBUTE_ALREADY_EXIST);
    }

    public static AttributeAlreadyExistException withId(UUID id) {
        AttributeAlreadyExistException exception = new AttributeAlreadyExistException();
        exception.addDetail("attributeId", id);
        return exception;
    }
    public static AttributeAlreadyExistException withName(String name) {
        AttributeAlreadyExistException exception = new AttributeAlreadyExistException();
        exception.addDetail("name", name);
        return exception;
    }
}
