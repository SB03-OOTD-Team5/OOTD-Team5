package com.sprint.ootd5team.domain.feed.dto.enums;

import org.springframework.data.domain.Sort;

public enum SortDirection {
    ASCENDING,
    DESCENDING;

    public Sort.Direction toSpringDirection() {
        return this == ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}