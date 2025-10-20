package com.sprint.ootd5team.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.ootd5team.base.exception.feed.InvalidSortOptionException;
import com.sprint.ootd5team.domain.feed.dto.data.FeedSearchResult;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.search.FeedDocument;
import com.sprint.ootd5team.domain.feed.search.FeedSearchService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedSearchService 슬라이스 테스트")
public class FeedSearchServiceTest {

    @Mock
    private ElasticsearchOperations operations;

    @InjectMocks
    private FeedSearchService service;

    private FeedListRequest request;

    @BeforeEach
    void setUp() {
        request = new FeedListRequest(
            null, null, 2, "createdAt",
            SortDirection.DESCENDING, "피드", null, null, null
        );
    }

    @Test
    @DisplayName("키워드 검색 성공 - hasNext=false")
    void searchByKeyword_success() {
        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        FeedDocument doc1 = FeedDocument.builder()
            .feedId(id1)
            .content("내용1")
            .likeCount(5L)
            .createdAt(Instant.now())
            .build();

        FeedDocument doc2 = FeedDocument.builder()
            .feedId(id2)
            .content("내용2")
            .likeCount(10L)
            .createdAt(Instant.now().minusSeconds(10))
            .build();

        SearchHit<FeedDocument> hit1 = new SearchHit<>(
            "feeds-v5", null, null, 1.0f, null,
            null, null, null, null, null, doc1
        );

        SearchHit<FeedDocument> hit2 = new SearchHit<>(
            "feeds-v5", null, null, 1.0f, null,
            null, null, null, null, null, doc2
        );

        SearchHits<FeedDocument> hits = new SearchHitsImpl<>(
            2L, null, 1.0f, Duration.ofMillis(10),
            null, null, List.of(hit1, hit2), null, null, null
        );

        when(operations.search(any(NativeQuery.class), eq(FeedDocument.class)))
            .thenReturn(hits);

        // when
        FeedSearchResult result = service.searchByKeyword(request);

        // then
        assertThat(result.feedIds()).containsExactly(id1, id2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalCount()).isEqualTo(2L);

        verify(operations).search(any(NativeQuery.class), eq(FeedDocument.class));
    }

    @Test
    @DisplayName("search_after 커서 적용 시 builder 로그 생성 확인")
    void searchByKeyword_withCursor() {
        // given
        FeedListRequest feedListRequest = new FeedListRequest(
            Instant.now().toString(), UUID.randomUUID(), 2, "createdAt",
            SortDirection.DESCENDING, "패션", null, null, null
        );

        UUID id1 = UUID.randomUUID();

        FeedDocument doc = FeedDocument.builder()
            .feedId(id1)
            .content("내용1")
            .likeCount(5L)
            .createdAt(Instant.now())
            .build();

        SearchHit<FeedDocument> hit = new SearchHit<>(
            "feeds-v5", null, null, 1.0f, null,
            null, null, null, null, null, doc
        );

        SearchHits<FeedDocument> hits = new SearchHitsImpl<>(
            2L, null, 1.0f, Duration.ofMillis(10),
            null, null, List.of(hit), null, null, null
        );

        when(operations.search(any(NativeQuery.class), eq(FeedDocument.class)))
            .thenReturn(hits);

        // when
        FeedSearchResult result = service.searchByKeyword(feedListRequest);

        // then
        assertThat(result.feedIds()).contains(id1);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("잘못된 sortBy 값이면 InvalidSortOptionException 발생")
    void searchByKeyword_invalidSort() {
        // given
        FeedListRequest badRequest = new FeedListRequest(
            "cursor", UUID.randomUUID(), 10, "wrongField",
            null, "피드", null, null, null
        );

        // when & then
        assertThatThrownBy(() -> service.searchByKeyword(badRequest))
            .isInstanceOf(InvalidSortOptionException.class);
    }
}