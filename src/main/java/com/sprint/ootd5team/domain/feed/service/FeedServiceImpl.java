package com.sprint.ootd5team.domain.feed.service;

import com.sprint.ootd5team.base.exception.feed.InvalidSortOptionException;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.feed.assembler.FeedDtoAssembler;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.entity.FeedClothes;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.feed.validator.FeedValidator;
import com.sprint.ootd5team.domain.follow.repository.FollowRepository;
import com.sprint.ootd5team.domain.notification.event.type.multi.FeedCreatedEvent;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedClothesRepository feedClothesRepository;
    private final FeedDtoAssembler feedDtoAssembler;

    private final ApplicationEventPublisher eventPublisher;
    private final FollowRepository followRepository;
    private final FeedValidator feedValidator;

    /**
     * 새 피드를 생성한다.
     *
     * <p>
     * - 작성자, 날씨, 옷 ID의 유효성을 검증한다.<br>
     * - 피드와 피드-옷 매핑을 저장한다.<br>
     * - 생성된 피드의 이벤트를 발행한다.
     * </p>
     *
     * @param request       피드 생성 요청
     * @param currentUserId 현재 로그인 사용자 ID
     * @return 생성된 피드 DTO
     */
    @Transactional
    @Override
    public FeedDto create(FeedCreateRequest request, UUID currentUserId) {
        log.info("[FeedService] 피드 등록 시작");

        UUID authorId = request.authorId();
        UUID weatherId = request.weatherId();
        Set<UUID> clothesIds = request.clothesIds();

        feedValidator.validateAuthorAndWeather(authorId, weatherId);
        List<Clothes> clothesList = feedValidator.validateClothes(clothesIds);

        Feed feed = saveFeed(authorId, weatherId, request.content());
        saveFeedClothes(feed, clothesList);

        FeedDto dto = feedRepository.findFeedDtoById(feed.getId(), currentUserId);
        publishFeedCreatedEvent(dto);

        return feedDtoAssembler.enrich(List.of(dto)).get(0);
    }

    /**
     * 피드 목록을 커서 기반 페이지네이션으로 조회한다.
     * <p>
     * - 조건에 맞는 피드 목록을 조회하고, limit+1 규칙으로 hasNext 여부를 판별한다.<br>
     * - 마지막 피드를 기준으로 nextCursor / nextIdAfter 값을 계산한다.<br>
     * - 전체 개수를 조회하고, {@link FeedDtoAssembler}로 DTO를 가공한다.<br>
     * - 최종적으로 {@link FeedDtoCursorResponse} 응답을 반환한다.
     * </p>
     *
     * @param request       조회 조건 및 페이지네이션 정보
     * @param currentUserId 현재 로그인 사용자 ID (likedByMe 여부 판별에 사용)
     * @return 커서 기반 페이지네이션 응답
     */
    @Override
    public FeedDtoCursorResponse getFeeds(FeedListRequest request, UUID currentUserId) {
        log.info("[FeedService] 피드 목록 조회 시작 - userId:{}", currentUserId);

        List<FeedDto> feedDtos = feedRepository.findFeedDtos(request, currentUserId);

        boolean hasNext = feedDtos.size() > request.limit();
        if (hasNext) {
            feedDtos = feedDtos.subList(0, request.limit());
        }

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (hasNext && !feedDtos.isEmpty()) {
            FeedDto lastFeedDto = feedDtos.get(feedDtos.size() - 1);

            switch (request.sortBy()) {
                case "createdAt" -> nextCursor = lastFeedDto.createdAt().toString();
                case "likeCount" -> nextCursor = String.valueOf(lastFeedDto.likeCount());
                default -> throw InvalidSortOptionException.withSortBy(request.sortBy());
            }
            nextIdAfter = lastFeedDto.id();
        }
        log.debug("[FeedService] nextCursor:{}, nextIdAfter:{}", nextCursor, nextIdAfter);

        long totalCount = feedRepository.countFeeds(
            request.keywordLike(),
            request.skyStatusEqual(),
            request.precipitationTypeEqual(),
            request.authorIdEqual()
        );
        log.debug("[FeedService] totalCount:{}", totalCount);

        List<FeedDto> data = feedDtoAssembler.enrich(feedDtos);

        return new FeedDtoCursorResponse(
            data,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            request.sortBy(),
            request.sortDirection().name()
        );
    }

    @Override
    public FeedDto getFeed(UUID feedId, UUID currentUserId) {
        log.info("[FeedService] 피드 조회 - feedId:{}, currentUserId:{}", feedId, currentUserId);

        feedValidator.getFeedOrThrow(feedId);
        FeedDto dto = feedRepository.findFeedDtoById(feedId, currentUserId);

        return feedDtoAssembler.enrich(List.of(dto)).get(0);
    }

    /**
     * 주어진 feedId에 해당하는 피드를 수정한다.
     *
     * @param feedId        수정 대상 피드 ID
     * @param request       수정 요청 객체
     * @param currentUserId 현재 로그인 사용자 ID (likedByMe 여부 판단에 사용)
     * @return 수정된 최신 상태의 {@link FeedDto}
     */
    @Transactional
    @Override
    public FeedDto update(UUID feedId, FeedUpdateRequest request, UUID currentUserId) {
        log.info("[FeedService] 피드 수정 시작 - userId:{}", currentUserId);

        Feed feed = feedValidator.getFeedOrThrow(feedId);
        feed.updateContent(request.content());

        log.debug("[FeedService] 피드 수정 완료 - feedId:{}, newContent:{}", feedId, feed.getContent());

        FeedDto updated = feedRepository.findFeedDtoById(feedId, currentUserId);
        return feedDtoAssembler.enrich(List.of(updated)).get(0);
    }

    /**
     * 주어진 feedId에 해당하는 피드를 삭제한다.
     *
     * <p>DB 제약조건(FK + ON DELETE CASCADE)에 의해 연결된 엔티티 (피드 댓글, 좋아요, OOTD 매핑)도 자동 삭제</p>
     *
     * @param feedId 삭제할 피드 ID
     */
    @Transactional
    @Override
    public void delete(UUID feedId) {
        log.info("[FeedService] 피드 삭제 시작");

        Feed feed = feedValidator.getFeedOrThrow(feedId);

        feedRepository.delete(feed);
    }

    private void publishFeedCreatedEvent(FeedDto dto) {
        // authorId = followeeId 로 followerIds 가져오기
        List<UUID> followerIds = followRepository.findFollowerIds(dto.author().userId());

        eventPublisher.publishEvent(new FeedCreatedEvent(
            dto.id(),
            dto.author().userId(),
            dto.author().name(),
            dto.content(),
            followerIds
        ));
    }

    private Feed saveFeed(UUID authorId, UUID weatherId, String content) {
        Feed feed = Feed.of(
            authorId, weatherId, content
        );

        feedRepository.save(feed);
        log.debug("[FeedService] 저장된 Feed 엔티티 - Feed:{}", feed);

        return feed;
    }

    private void saveFeedClothes(Feed feed, List<Clothes> clothesList) {
        List<FeedClothes> mappings = clothesList.stream()
            .map(clothes -> {
                log.debug("[FeedService] FeedClothes 엔티티 생성 - feedId:{}, clothesId:{}",
                    feed.getId(), clothes.getId());
                return new FeedClothes(feed.getId(), clothes.getId());
            })
            .toList();

        feedClothesRepository.saveAll(mappings);
    }
}