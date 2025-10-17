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
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch에서 피드 콘텐츠를 키워드로 검색하는 서비스.
 *
 * <p>검색어를 기준으로 ngram 필드와 원문 필드를 함께 조회하며,
 * 커서 기반 페이지네이션(search_after)을 지원한다.</p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FeedSearchService {

    private final ElasticsearchOperations operations;

    /**
     * 키워드 기반 피드 검색을 수행한다.
     */
    public FeedSearchResult searchByKeyword(FeedListRequest request) {
        log.info("[FeedSearchService] ES 피드 검색 시작 - keyword: {}", request.keywordLike());

        var query = buildQuery(request);
        var hits = operations.search(query, FeedDocument.class);

        var feedIds = hits.getSearchHits().stream()
            .limit(request.limit())
            .map(hit -> hit.getContent().getFeedId())
            .toList();

        var total = hits.getTotalHits();
        var hasNext = hits.getSearchHits().size() > request.limit();

        var nextCursorInfo = hasNext
            ? extractNextCursor(hits, request.limit(), request.sortBy())
            : null;

        log.info("[FeedSearchService] 검색 결과: totalHits={}, feedCount={}, hasNext={}",
            total, feedIds.size(), hasNext);

        return new FeedSearchResult(
            feedIds,
            nextCursorInfo != null ? nextCursorInfo.cursor() : null,
            nextCursorInfo != null ? nextCursorInfo.idAfter() : null,
            hasNext,
            total
        );
    }

    /**
     * Feed 검색용 Elasticsearch 쿼리를 구성한다.
     */
    private NativeQuery buildQuery(FeedListRequest request) {
        Sort sort = Sort.by(
            Sort.Order.desc(request.sortBy()),
            Sort.Order.desc("feedId")
        );

        NativeQueryBuilder builder = new NativeQueryBuilder()
            .withQuery(q -> q.multiMatch(m -> m
                .fields(List.of("content", "content.ngram"))
                .query(request.keywordLike())
                .type(TextQueryType.BestFields)
                .operator(Operator.Or)
            ))
            .withSort(sort)
            .withPageable(PageRequest.of(0, request.limit() + 1));

        if (request.cursor() != null && request.idAfter() != null) {
            Object cursorValue = parseCursor(request.sortBy(), request.cursor());
            builder.withSearchAfter(List.of(cursorValue, request.idAfter().toString()));

            log.debug("[FeedSearchService] search_after 적용 - cursor={}, idAfter={}",
                cursorValue, request.idAfter());
        }

        return builder.build();
    }

    /**
     * 다음 페이지 요청에 사용할 커서 정보를 추출한다.
     */
    private NextCursor extractNextCursor(SearchHits<FeedDocument> hits, int limit, String sortBy) {
        var sortValues = hits.getSearchHits().get(limit - 1).getSortValues();
        String nextCursor;

        if ("createdAt".equals(sortBy)) {
            long millis = ((Number) sortValues.get(0)).longValue();
            nextCursor = Instant.ofEpochMilli(millis).toString();
        } else {
            nextCursor = sortValues.get(0).toString();
        }

        UUID nextIdAfter = UUID.fromString(sortValues.get(1).toString());

        return new NextCursor(nextCursor, nextIdAfter);
    }

    private Object parseCursor(String sortBy, String raw) {
        return switch (sortBy) {
            case "likeCount" -> Long.parseLong(raw);
            case "createdAt" -> Instant.parse(raw).toEpochMilli();
            default -> throw InvalidSortOptionException.withSortBy(sortBy);
        };
    }

    private record NextCursor(String cursor, UUID idAfter) {}
}