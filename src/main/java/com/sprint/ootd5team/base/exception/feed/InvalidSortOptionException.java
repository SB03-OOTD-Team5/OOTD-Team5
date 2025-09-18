package com.sprint.ootd5team.base.exception.feed;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class InvalidSortOptionException extends FeedException {

    public InvalidSortOptionException() { super(ErrorCode.INVALID_SORT_OPTION); }

    public static InvalidSortOptionException withSortBy (String sortBy) {
        InvalidSortOptionException exception = new InvalidSortOptionException();
        exception.addDetail("sortBy", sortBy);
        return exception;
    }
}