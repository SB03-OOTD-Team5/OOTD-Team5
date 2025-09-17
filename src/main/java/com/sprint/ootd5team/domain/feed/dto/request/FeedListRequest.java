package com.sprint.ootd5team.domain.feed.dto.request;

import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record FeedListRequest(
    String cursor,
    UUID idAfter,
    @NotNull int limit,
    @NotBlank @Pattern(regexp = "createdAt|likeCount") String sortBy,
    @NotNull SortDirection sortDirection,
    String keywordLike,
    SkyStatus skyStatusEqual,
    PrecipitationType precipitationTypeEqual,
    UUID authorIdEqual
) { }