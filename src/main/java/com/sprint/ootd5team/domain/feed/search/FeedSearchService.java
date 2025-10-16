package com.sprint.ootd5team.domain.feed.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.sprint.ootd5team.base.exception.feed.InvalidSortOptionException;
import com.sprint.ootd5team.domain.feed.dto.data.FeedSearchResult;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class FeedSearchService {

    private final ElasticsearchOperations operations;

    public FeedSearchResult searchByKeyword(FeedListRequest request) {
        log.info("[FeedSearchService] ES 피드 검색 시작 - keyword: {}", request.keywordLike());

        Sort sort = Sort.by(
            new Order(Sort.Direction.DESC, request.sortBy()),
            new Order(Sort.Direction.DESC, "feedId")
        );

        NativeQueryBuilder builder = new NativeQueryBuilder()
            .withQuery(q -> q
                .multiMatch(m -> m
                    .fields(List.of("content", "content.ngram"))
                    .query(request.keywordLike())
                    .type(TextQueryType.BestFields)
                    .operator(Operator.Or)
                )
            )
            .withSort(sort)
            .withPageable(PageRequest.of(0, request.limit() + 1));

        if (request.cursor() != null && request.idAfter() != null) {
            Object cursorValue = parseCursor(request.sortBy(), request.cursor());
            Object idAfter = request.idAfter().toString();

            builder.withSearchAfter(List.of(cursorValue, idAfter));
            log.debug("[FeedSearchService] search_after 적용 - cursor: {}, idAfter: {}", cursorValue, idAfter);
        }

        SearchHits<FeedDocument> hits = operations.search(builder.build(), FeedDocument.class);
        long total = hits.getTotalHits();
        List<SearchHit<FeedDocument>> hitList = hits.getSearchHits();

        boolean hasNext = hitList.size() > request.limit();
        List<SearchHit<FeedDocument>> trimmedHits = hasNext ? hitList.subList(0, request.limit()) : hitList;

        List<UUID> feedIds = trimmedHits.stream()
            .map(hit -> hit.getContent().getFeedId())
            .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext) {
            List<Object> sortValues = hitList.get(request.limit() - 1).getSortValues();

            if ("createdAt".equals(request.sortBy())) {
                long millis = ((Number) sortValues.get(0)).longValue();
                nextCursor = Instant.ofEpochMilli(millis).toString();
            } else {
                nextCursor = sortValues.get(0).toString();
            }

            nextIdAfter = UUID.fromString(sortValues.get(1).toString());
        }

        log.info("[FeedSearchService] 검색 결과: totalHits={}, 반환된 feedIds={}", hits.getTotalHits(), feedIds.size());

        return new FeedSearchResult(feedIds, nextCursor, nextIdAfter, hasNext, total);
    }

    private Object parseCursor(String sortBy, String raw) {
        return switch (sortBy) {
            case "likeCount" -> Long.parseLong(raw);
            case "createdAt" -> Instant.parse(raw).toEpochMilli();
            default -> throw InvalidSortOptionException.withSortBy(sortBy);
        };
    }
}