package com.sprint.ootd5team.domain.feed.dto.enums;

public enum FeedSortBy {
    CREATED_AT("createdAt"),
    LIKE_COUNT("likeCount");

    private final String columnName;

    FeedSortBy(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }
}