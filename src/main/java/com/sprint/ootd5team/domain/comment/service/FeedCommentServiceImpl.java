package com.sprint.ootd5team.domain.comment.service;

import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.comment.dto.data.CommentDto;
import com.sprint.ootd5team.domain.comment.dto.request.CommentCreateRequest;
import com.sprint.ootd5team.domain.comment.dto.request.CommentListRequest;
import com.sprint.ootd5team.domain.comment.dto.response.CommentDtoCursorResponse;
import com.sprint.ootd5team.domain.comment.entity.FeedComment;
import com.sprint.ootd5team.domain.comment.mapper.FeedCommentMapper;
import com.sprint.ootd5team.domain.comment.repository.FeedCommentRepository;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.profile.entity.Profile;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FeedCommentServiceImpl implements FeedCommentService {

    private final FeedCommentRepository feedCommentRepository;
    private final FeedRepository feedRepository;
    private final ProfileRepository profileRepository;
    private final FeedCommentMapper feedCommentMapper;

    /**
     * 피드에 달린 댓글 목록을 커서 기반 페이지네이션으로 조회한다.
     *
     * @param feedId  조회할 피드 ID
     * @param request 커서, limit 등 페이지네이션 요청 파라미터
     * @return 댓글 목록과 커서 정보를 담은 응답 DTO
     * @throws FeedNotFoundException 피드가 존재하지 않을 경우
     */
    @Override
    public CommentDtoCursorResponse getComments(UUID feedId, CommentListRequest request) {
        log.info("[FeedCommentService] 댓글 목록 조회 요청 시작 - feedId={}", feedId);

        validateFeed(feedId);

        Instant createdAtCursor = request.cursor() != null ? Instant.parse(request.cursor()) : null;
        UUID idCursor = request.idAfter();

        List<CommentDto> comments = feedCommentRepository.findByFeedIdWithCursor(
            feedId,
            createdAtCursor,
            idCursor,
            request.limit()
        );

        boolean hasNext = comments.size() > request.limit();
        if (hasNext) {
            comments = comments.subList(0, request.limit());
        }

        CommentDto last = comments.isEmpty() ? null : comments.get(comments.size() - 1);

        return new CommentDtoCursorResponse(
            comments,
            last != null ? last.createdAt().toString() : null,
            last != null ? last.id() : null,
            hasNext,
            feedCommentRepository.countByFeedId(feedId),
            "createdAt",
            "DESCENDING"
        );
    }

    /**
     * 새로운 댓글을 생성한다.
     *
     * @param feedId              댓글이 달릴 피드 ID
     * @param commentCreateRequest 댓글 생성 요청 (작성자 ID, 내용 포함)
     * @return 저장된 댓글 DTO
     * @throws FeedNotFoundException    피드가 존재하지 않을 경우
     * @throws ProfileNotFoundException 작성자 프로필이 없을 경우
     */
    @Override
    @Transactional
    public CommentDto create(UUID feedId, CommentCreateRequest commentCreateRequest) {
        UUID authorId = commentCreateRequest.authorId();

        log.info("[FeedCommentService] 댓글 등록 요청 시작 - "
            + "feedId={}, content={}", feedId, commentCreateRequest.content());

        validateFeed(feedId);

        Profile profile = profileRepository.findByUserId(authorId)
            .orElseThrow(() -> ProfileNotFoundException.withUserId(authorId));

        FeedComment feedComment = new FeedComment(feedId, authorId, commentCreateRequest.content());
        FeedComment saved = feedCommentRepository.save(feedComment);
        log.debug("[FeedCommentService] 저장된 FeedComment: {}", saved);

        feedRepository.incrementCommentCount(feedId);

        return feedCommentMapper.toDto(saved, profile);
    }

    private void validateFeed(UUID feedId) {
        if (!feedRepository.existsById(feedId)) {
            log.warn("[FeedCommentService] 유효하지 않은 피드 - feedId:{}", feedId);
            throw FeedNotFoundException.withId(feedId);
        }
    }
}