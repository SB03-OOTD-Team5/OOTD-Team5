package com.sprint.ootd5team.base.exception.clothesattribute;

import com.sprint.ootd5team.base.errorcode.ErrorCode;

public class InvalidAttributeException extends AttributeException {

	public InvalidAttributeException() {
		super(ErrorCode.INVALID_ATTRIBUTE_NAME);
	}
}
