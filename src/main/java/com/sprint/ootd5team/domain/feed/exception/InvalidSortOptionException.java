package com.sprint.ootd5team.domain.feed.exception;

import com.sprint.ootd5team.base.errorcode.ErrorCode;
import com.sprint.ootd5team.base.exception.OotdException;
import lombok.Getter;

@Getter
public class InvalidSortOptionException extends OotdException {

    private final String sortBy;

    public InvalidSortOptionException(String sortBy) {
        super(ErrorCode.INVALID_SORT_OPTION);
        this.sortBy = sortBy;
        this.addDetail("sortBy", sortBy);
    }
}