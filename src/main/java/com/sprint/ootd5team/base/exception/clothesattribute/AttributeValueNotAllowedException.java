package com.sprint.ootd5team.base.exception.clothesattribute;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import java.util.UUID;

public class AttributeValueNotAllowedException extends AttributeException {
    public AttributeValueNotAllowedException() {
        super(ErrorCode.CLOTHES_ATTRIBUTE_VALUE_NOT_ALLOWED);
    }

    public static AttributeValueNotAllowedException withValue(UUID attributeId, String value) {
        AttributeValueNotAllowedException ex = new AttributeValueNotAllowedException();
        ex.addDetail("attributeId", attributeId);
        ex.addDetail("value", value);
        return ex;
    }

}
