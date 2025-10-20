package com.sprint.ootd5team.domain.feed.indexer;

import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedDeletedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedIndexCreatedEvent;
import com.sprint.ootd5team.domain.feed.event.type.FeedLikeCountUpdateEvent;
import com.sprint.ootd5team.domain.feed.search.FeedDocument;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

/**
 * Kafka로부터 전달받은 피드 관련 이벤트를 기반으로
 * Elasticsearch 인덱스를 생성·수정·삭제하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchFeedIndexer {

    private final ElasticsearchOperations operations;

    @Value("${spring.elasticsearch.indices.feed}")
    private String indexName;

    /**
     * 피드 생성 이벤트를 기반으로 새로운 Elasticsearch 문서를 생성합니다.
     */
    public void create(FeedIndexCreatedEvent event) {
        FeedDocument document = FeedDocument.builder()
            .feedId(event.getFeedId())
            .content(event.getContent())
            .createdAt(event.getCreatedAt())
            .build();

        operations.save(document);

        log.info("[ElasticsearchFeedIndexer] Feed 인덱싱 완료: {}", event.getFeedId());
    }

    /**
     * 피드 내용 수정 이벤트를 기반으로 Elasticsearch 문서의 content 필드를 업데이트합니다.
     */
    public void updateContent(FeedContentUpdatedEvent event) {
        Map<String, Object> doc = Map.of("content", event.getContent());
        UpdateQuery query = UpdateQuery.builder(event.getFeedId().toString())
            .withDocument(Document.from(doc))
            .build();

        operations.update(query, IndexCoordinates.of(indexName));
        log.info("[ElasticsearchFeedIndexer] content 업데이트 완료: {}", event.getFeedId());
    }

    /**
     * 좋아요 수 변경 이벤트를 기반으로 Elasticsearch 문서의 likeCount 필드를 업데이트합니다.
     */

    public void updateLikeCount(FeedLikeCountUpdateEvent event) {
        Map<String, Object> doc = Map.of("likeCount", event.getNewLikeCount());
        UpdateQuery query = UpdateQuery.builder(event.getFeedId().toString())
            .withDocument(Document.from(doc))
            .build();

        operations.update(query, IndexCoordinates.of(indexName));
        log.info("[ElasticsearchFeedIndexer] likeCount 업데이트 완료: {}", event.getFeedId());
    }

    /**
     * 피드 삭제 이벤트를 기반으로 Elasticsearch 인덱스에서 해당 문서를 제거합니다.
     */
    public void delete(FeedDeletedEvent event) {
        operations.delete(event.getFeedId().toString(), IndexCoordinates.of(indexName));
        log.info("[ElasticsearchFeedIndexer] 인덱스 삭제 완료: {}", event.getFeedId());
    }
}