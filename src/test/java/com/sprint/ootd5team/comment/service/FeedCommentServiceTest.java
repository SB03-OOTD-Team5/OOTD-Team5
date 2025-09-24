package com.sprint.ootd5team.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.dto.request.CommentListRequest;
import com.sprint.ootd5team.domain.comment.dto.response.CommentDtoCursorResponse;
import com.sprint.ootd5team.domain.comment.repository.FeedCommentRepository;
import com.sprint.ootd5team.domain.comment.service.FeedCommentServiceImpl;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedCommentService 슬라이스 테스트")
@ActiveProfiles("test")
public class FeedCommentServiceTest {

    @Mock
    private FeedCommentRepository feedCommentRepository;

    @Mock
    private FeedRepository feedRepository;

    @InjectMocks
    private FeedCommentServiceImpl feedCommentService;

    @Test
    @DisplayName("댓글 목록 조회 성공 - 데이터가 존재하는 경우")
    void getComments_success_withData() {
        // given
        UUID feedId = UUID.randomUUID();

        CommentListRequest request = new CommentListRequest(
            "2025-09-20T09:00:00Z",
            UUID.randomUUID(),
            10
        );

        AuthorDto author1 = new AuthorDto(UUID.randomUUID(), "작성자1", "profile1.png");
        AuthorDto author2 = new AuthorDto(UUID.randomUUID(), "작성자2", "profile2.png");

        CommentDto comment1 = new CommentDto(
            UUID.randomUUID(),
            Instant.parse("2025-09-20T09:30:00Z"),
            feedId,
            author1,
            "댓글 내용 1"
        );
        CommentDto comment2 = new CommentDto(
            UUID.randomUUID(),
            Instant.parse("2025-09-20T09:40:00Z"),
            feedId,
            author2,
            "댓글 내용 2"
        );

        given(feedRepository.existsById(feedId)).willReturn(true);
        given(feedCommentRepository.findByFeedIdWithCursor(eq(feedId), any(), any(), eq(10)))
            .willReturn(List.of(comment1, comment2));
        given(feedCommentRepository.countByFeedId(feedId)).willReturn(2L);

        // when
        CommentDtoCursorResponse response = feedCommentService.getComments(feedId, request);

        // then
        assertThat(response.data()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(2L);
        assertThat(response.nextCursor()).isEqualTo(comment2.createdAt().toString());
        assertThat(response.nextIdAfter()).isEqualTo(comment2.id());
        assertThat(response.sortBy()).isEqualTo("createdAt");
        assertThat(response.sortDirection()).isEqualTo("DESCENDING");
    }

    @Test
    @DisplayName("댓글 목록 조회 실패 - 존재하지 않는 feedId")
    void getComments_fail_feedNotFound() {
        // given
        UUID feedId = UUID.randomUUID();
        CommentListRequest request = new CommentListRequest(null, null, 10);

        given(feedRepository.existsById(feedId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> feedCommentService.getComments(feedId, request))
            .isInstanceOf(FeedNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 - 결과가 비어있는 경우")
    void getComments_success_empty() {
        // given
        UUID feedId = UUID.randomUUID();
        CommentListRequest request = new CommentListRequest(null, null, 10);

        given(feedRepository.existsById(feedId)).willReturn(true);
        given(feedCommentRepository.findByFeedIdWithCursor(eq(feedId), any(), any(), eq(10)))
            .willReturn(List.of()); // empty
        given(feedCommentRepository.countByFeedId(feedId)).willReturn(0L);

        // when
        CommentDtoCursorResponse response = feedCommentService.getComments(feedId, request);

        // then
        assertThat(response.data()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalCount()).isEqualTo(0L);
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextIdAfter()).isNull();
    }
}