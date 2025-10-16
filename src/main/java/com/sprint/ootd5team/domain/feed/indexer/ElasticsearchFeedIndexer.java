package com.sprint.ootd5team.domain.feed.indexer;

import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.search.FeedDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchFeedIndexer {

    private final ElasticsearchOperations operations;

    public void index(FeedIndexCreatedEvent event) {
        FeedDocument document = FeedDocument.builder()
            .feedId(event.getFeedId())
            .content(event.getContent())
            .createdAt(event.getCreatedAt().toString())
            .build();

        operations.save(document);

        log.info("[ElasticsearchFeedIndexer] Feed 인덱싱 완료: {}", event.getFeedId());
    }
}