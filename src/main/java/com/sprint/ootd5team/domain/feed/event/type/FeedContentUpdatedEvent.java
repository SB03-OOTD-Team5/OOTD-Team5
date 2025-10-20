package com.sprint.ootd5team.domain.feed.event.type;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("feed-updated")
public class FeedContentUpdatedEvent {

    private UUID feedId;
    private String content;
}