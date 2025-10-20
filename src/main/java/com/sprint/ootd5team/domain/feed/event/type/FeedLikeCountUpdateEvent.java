package com.sprint.ootd5team.domain.feed.event.type;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class FeedLikeCountUpdateEvent {

    private UUID feedId;
    private long newLikeCount;
}
