package com.sprint.ootd5team.domain.feed.service;

import com.sprint.ootd5team.base.exception.clothes.ClothesNotFoundException;
import com.sprint.ootd5team.base.exception.feed.FeedNotFoundException;
import com.sprint.ootd5team.base.exception.feed.InvalidSortOptionException;
import com.sprint.ootd5team.base.exception.profile.ProfileNotFoundException;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.dto.request.FeedCreateRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.dto.request.FeedUpdateRequest;
import com.sprint.ootd5team.domain.feed.dto.response.FeedDtoCursorResponse;
import com.sprint.ootd5team.domain.feed.entity.Feed;
import com.sprint.ootd5team.domain.feed.entity.FeedClothes;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.profile.repository.ProfileRepository;
import com.sprint.ootd5team.domain.weather.exception.WeatherNotFoundException;
import com.sprint.ootd5team.domain.weather.repository.WeatherRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final ProfileRepository profileRepository;
    private final WeatherRepository weatherRepository;
    private final ClothesRepository clothesRepository;
//    private final NotificationService notificationService;
//    private final UserRepository userRepository;
//    private final FollowRepository followRepository;

    /**
     * 피드를 생성하고 {@link FeedDto}로 반환한다.
     *
     * <p>작성자, 날씨, 옷 ID 유효성을 검증한 뒤 피드와 피드/옷 매핑을 저장한다.</p>
     *
     * @param request       피드 생성 요청
     * @param currentUserId 현재 로그인한 사용자 ID
     * @return 생성된 피드 DTO
     */
    @Transactional
    @Override
    public FeedDto create(FeedCreateRequest request, UUID currentUserId) {
        log.info("[FeedService] 피드 등록 시작");

        UUID authorId = request.authorId();
        UUID weatherId = request.weatherId();
        Set<UUID> clothesIds = request.clothesIds();

        validateAuthorAndWeather(authorId, weatherId);
        List<Clothes> clothesList = validateClothes(clothesIds);

        Feed feed = saveFeed(authorId, weatherId, request.content());
        saveFeedClothes(feed, clothesList);

        //TODO
//        // 팔로워 조회
//        List<UUID> followerIds = followRepository.findFollowerIds(authorId);
//
//        // 작성자 조회
//        User author = userRepository.findById(request.authorId())
//            .orElseThrow(() -> UserNotFoundException.withId(authorId));
//
//        // 알림 전송 (팔로워들 모두 수신자)
//        followerIds.forEach(followerId ->
//            notificationService.createByReceiverId(
//                followerId,
//                NotificationType.FEED_FOLLOW_CREATED,
//                NotificationLevel.INFO,
//                author.getName(),
//                feed.getContent()
//            )
//        );

        return enrichSingleFeed(
            feedRepository.findFeedDtoById(feed.getId(), currentUserId)
        );
    }

    /**
     * 피드 목록을 커서 기반 페이지네이션 방식으로 조회한다.
     *
     * <ul>
     *   <li>{@link FeedRepository#findFeedDtos(FeedListRequest, UUID)} 호출로 피드 목록을 조회</li>
     *   <li>limit+1 개를 조회한 뒤 초과분을 잘라내어 hasNext 여부 판별</li>
     *   <li>마지막 피드 기준으로 nextCursor, nextIdAfter 값을 계산</li>
     *   <li>조건에 맞는 전체 피드 개수를 count 쿼리로 조회</li>
     *   <li>{@link #enrichWithOotds(List)} 호출로 각 피드에 OOTD 데이터를 매핑</li>
     *   <li>최종적으로 {@link FeedDtoCursorResponse} 형태로 응답 조립</li>
     * </ul>
     *
     * @param request       조회 조건 및 페이지네이션 정보 (cursor, limit, 정렬, 필터 조건 포함)
     * @param currentUserId 현재 로그인한 사용자 ID (likedByMe 여부 계산에 사용)
     * @return 커서 기반 페이지네이션 응답 객체 {@link FeedDtoCursorResponse}
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

        List<FeedDto> data = enrichWithOotds(feedDtos);

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

        FeedDto dto = feedRepository.findFeedDtoById(feedId, currentUserId);
        if (dto == null) {
            throw FeedNotFoundException.withId(feedId);
        }

        return enrichSingleFeed(dto);
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

        Feed feed = getFeedOrThrow(feedId);
        feed.updateContent(request.content());

        log.debug("[FeedService] 피드 수정 완료 - feedId:{}, newContent:{}", feedId, feed.getContent());

        FeedDto updated = feedRepository.findFeedDtoById(feedId, currentUserId);
        return enrichSingleFeed(updated);
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

        Feed feed = getFeedOrThrow(feedId);

        feedRepository.delete(feed);
    }

    /**
     * 조회된 피드 목록에 OOTD 데이터를 매핑한다.
     *
     * <p>feedIds를 기준으로 batch 조회하여 N+1 문제를 방지하며,
     * 각 FeedDto에 연관된 OOTD 목록을 채워 반환한다.</p>
     *
     * @param feedDtos OOTD 매핑 전의 피드 목록
     * @return OOTD 데이터가 매핑된 새로운 {@link FeedDto} 목록
     */
    private List<FeedDto> enrichWithOotds(List<FeedDto> feedDtos) {
        Map<UUID, List<OotdDto>> ootdsMap =
            feedClothesRepository.findOotdsByFeedIds(feedDtos.stream().map(FeedDto::id).toList());

        return feedDtos.stream()
            .map(feed -> feed.withOotds(ootdsMap.getOrDefault(feed.id(), List.of())))
            .toList();
    }

    /**
     * 단건 FeedDto에 OOTD 데이터를 매핑한다.
     *
     * <p>내부적으로 enrichWithOotds를 재사용.</p>
     */
    private FeedDto enrichSingleFeed(FeedDto feedDto) {
        return enrichWithOotds(List.of(feedDto)).get(0);
    }

    private Feed getFeedOrThrow(UUID feedId) {
        log.debug("[FeedService] 피드 조회 feedId:{}", feedId);

        return feedRepository.findById(feedId)
            .orElseThrow(() -> {
                log.warn("[FeedService] 유효하지 않은 피드 - feedId:{}", feedId);
                return FeedNotFoundException.withId(feedId);
            });
    }

    private void validateAuthorAndWeather(UUID authorId, UUID weatherId) {
        if (!profileRepository.existsByUserId(authorId)) {
            log.warn("[FeedService] 존재하지 않는 사용자 - userId:{}", authorId);
            throw ProfileNotFoundException.withUserId(authorId);
        } else if (!weatherRepository.existsById(weatherId)) {
            throw new WeatherNotFoundException("존재하지 않는 날씨 데이터입니다.");
        }
    }

    private List<Clothes> validateClothes(Set<UUID> clothesIds) {
        List<Clothes> clothesList = clothesRepository.findAllById(clothesIds);

        Set<UUID> foundIds = clothesList.stream()
            .map(Clothes::getId)
            .collect(Collectors.toSet());
        Set<UUID> missingIds = new HashSet<>(clothesIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            log.warn("[FeedService] 존재하지 않는 의상 - clothesIds:{}", missingIds);
            throw ClothesNotFoundException.withIds(missingIds);
        }

        return clothesList;
    }

    public Feed saveFeed(UUID authorId, UUID weatherId, String content) {
        Feed feed = Feed.of(
            authorId, weatherId, content
        );

        feedRepository.save(feed);
        log.debug("[FeedService] 저장된 Feed 엔티티 - Feed:{}", feed);

        return feed;
    }

    public void saveFeedClothes(Feed feed, List<Clothes> clothesList) {
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