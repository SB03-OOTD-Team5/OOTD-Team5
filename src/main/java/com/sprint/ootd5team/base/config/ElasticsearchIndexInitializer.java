package com.sprint.ootd5team.base.config;

import com.sprint.ootd5team.domain.feed.search.FeedDocument;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Component;

@Profile("dev")
@Slf4j
@RequiredArgsConstructor
@Component
public class ElasticsearchIndexInitializer {

    private final ElasticsearchOperations operations;

    @Value("${spring.elasticsearch.indices.feed}")
    private String indexName;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexIfNotExists() {
        IndexOperations indexOps = operations.indexOps(FeedDocument.class);

        if (!indexOps.exists()) {
            log.info("[ElasticsearchIndexInitializer] Elasticsearch 인덱스 생성 요청: {}", indexName);

            Map<String, Object> settings = Map.of(
                "index", Map.of(
                    "max_ngram_diff", 2,
                    "refresh_interval", "1s",
                    "number_of_replicas", 1,
                    "translog", Map.of(
                        "durability", "async"
                    )
                ),
                "analysis", Map.of(
                    "tokenizer", Map.of(
                        "nori_tokenizer", Map.of(
                            "type", "nori_tokenizer",
                            "decompound_mode", "mixed"
                        ),
                        "ngram_tokenizer", Map.of(
                            "type", "ngram",
                            "min_gram", 1,
                            "max_gram", 3
                        )
                    ),
                    "analyzer", Map.of(
                        "korean_nori_custom", Map.of(
                            "type", "custom",
                            "tokenizer", "nori_tokenizer",
                            "filter", List.of("nori_readingform", "lowercase")
                        ),
                        "ngram_analyzer", Map.of(
                            "type", "custom",
                            "tokenizer", "ngram_tokenizer",
                            "filter", List.of("lowercase")
                        )
                    )
                )
            );

            Document mapping = Document.create().append("properties", Map.of(
                "feedId", Map.of("type", "keyword"),
                "content", Map.of(
                    "type", "text",
                    "analyzer", "korean_nori_custom",
                    "search_analyzer", "ngram_analyzer",
                    "fields", Map.of(
                        "ngram", Map.of(
                            "type", "text",
                            "analyzer", "ngram_analyzer"
                        )
                    )
                ),
                "likeCount", Map.of("type", "long"),
                "createdAt", Map.of(
                    "type", "date",
                    "format", "strict_date_optional_time||epoch_millis"
                )
            ));

            try {
                indexOps.create(settings);
                indexOps.putMapping(mapping);
            } catch (Exception e) {
                log.warn("[ElasticsearchIndexInitializer] 이미 존재하거나 생성 실패", e);
            }

            log.info("[ElasticsearchIndexInitializer] Elasticsearch 인덱스 생성 완료: {}", indexName);
        } else {
            log.info("[ElasticsearchIndexInitializer] 이미 존재하는 Index: {}", indexName);
        }
    }
}