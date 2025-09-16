package com.sprint.ootd5team.domain.feed.exception;

import lombok.Getter;

@Getter
public class InvalidSortOptionException extends RuntimeException {

    private final String sortBy;

    public InvalidSortOptionException(String sortBy) {
        super("유효하지 않은 정렬 옵션입니다. sortBy: " + sortBy);
        this.sortBy = sortBy;
    }
}
