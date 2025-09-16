package com.sprint.ootd5team.domain.feed.repository.feed.impl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.ootd5team.domain.feed.dto.data.FeedDto;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.entity.QFeed;
import com.sprint.ootd5team.domain.feed.repository.feed.FeedRepositoryCustom;
import com.sprint.ootd5team.domain.like.entity.QFeedLike;
import com.sprint.ootd5team.domain.profile.entity.QProfile;
import com.sprint.ootd5team.domain.user.dto.AuthorDto;
import com.sprint.ootd5team.domain.user.entity.QUser;
import com.sprint.ootd5team.domain.weather.dto.data.PrecipitationDto;
import com.sprint.ootd5team.domain.weather.dto.data.TemperatureDto;
import com.sprint.ootd5team.domain.weather.dto.data.WeatherSummaryDto;
import com.sprint.ootd5team.domain.weather.entity.QWeather;
import com.sprint.ootd5team.domain.weather.enums.PrecipitationType;
import com.sprint.ootd5team.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * FeedRepositoryImpl
 *
 * QueryDSL을 사용해 Feed 관련 조회 쿼리를 구현한 클래스
 * 목록 조회, 단건 조회, 전체 카운트 조회 등
 */
@Slf4j
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 피드 목록 조회
     *
     * <p>조건(cursor, 정렬, 키워드, 날씨, 작성자)에 맞는 FeedDto 목록을 조회한다.
     * likedByMe 여부는 현재 사용자 ID를 기준으로 서브쿼리로 계산된다.
     * OOTD 데이터는 포함하지 않으며 서비스 계층에서 batch로 채운다.</p>
     *
     * @param request       페이지네이션 및 필터 조건을 담은 요청 객체
     * @param currentUserId 현재 로그인한 사용자 ID (likedByMe 계산용)
     * @return FeedDto 목록 (limit+1개 조회 → 서비스 계층에서 hasNext 판별)
     */
    @Override
    public List<FeedDto> findFeedDtos(FeedListRequest request, UUID currentUserId) {
        QFeed feed = QFeed.feed;
        QUser user = QUser.user;
        QProfile profile = QProfile.profile;
        QWeather weather = QWeather.weather;

        return queryFactory
            .select(feedProjection(currentUserId))
            .from(feed)
            .join(user).on(feed.authorId.eq(user.id))
            .leftJoin(profile).on(profile.userId.eq(user.id))
            .join(weather).on(feed.weatherId.eq(weather.id))
            .where(
                keywordLike(request.keywordLike()),
                skyStatusEq(request.skyStatusEqual()),
                precipitationEq(request.precipitationTypeEqual()),
                authorIdEq(request.authorIdEqual()),
                cursorAfter(request)
            )
            .orderBy(feedSort(request.sortBy(), request.sortDirection())
                .toArray(OrderSpecifier[]::new))
            .limit(request.limit() + 1)
            .fetch();
    }

    /**
     * 단일 피드 조회
     *
     * <p>feedId 기준으로 FeedDto를 조회한다.
     * OOTD 데이터는 포함하지 않으며 서비스 계층에서 batch로 채운다.</p>
     *
     * @param feedId        조회할 피드의 ID
     * @param currentUserId 현재 로그인한 사용자 ID (likedByMe 계산용)
     * @return FeedDto
     */
    @Override
    public FeedDto findFeedDtoById(UUID feedId, UUID currentUserId) {
        QFeed feed = QFeed.feed;
        QUser user = QUser.user;
        QProfile profile = QProfile.profile;
        QWeather weather = QWeather.weather;

        return queryFactory
            .select(feedProjection(currentUserId))
            .from(feed)
            .join(user).on(feed.authorId.eq(user.id))
            .leftJoin(profile).on(profile.userId.eq(user.id))
            .join(weather).on(feed.weatherId.eq(weather.id))
            .where(feed.id.eq(feedId))
            .fetchOne();
    }

    /**
     * 공통 FeedDto projection
     */
    private Expression<FeedDto> feedProjection(UUID currentUserId) {
        QFeed feed = QFeed.feed;
        QFeedLike feedLike = QFeedLike.feedLike;
        QUser user = QUser.user;
        QProfile profile = QProfile.profile;
        QWeather weather = QWeather.weather;

        return Projections.constructor(
            FeedDto.class,
            feed.id,
            feed.createdAt,
            feed.updatedAt,
            Projections.constructor(AuthorDto.class,
                user.id,
                user.name,
                profile.profileImageUrl
            ),
            Projections.constructor(WeatherSummaryDto.class,
                weather.id,
                weather.skyStatus,
                Projections.constructor(PrecipitationDto.class,
                    weather.precipitationType,
                    weather.precipitationAmount,
                    weather.precipitationProbability
                ),
                Projections.constructor(TemperatureDto.class,
                    weather.temperature,
                    weather.temperatureCompared,
                    weather.temperatureMin,
                    weather.temperatureMax
                )
            ),
            Expressions.constant(Collections.emptyList()),
            feed.content,
            feed.likeCount,
            feed.commentCount,
            JPAExpressions.selectOne()
                .from(feedLike)
                .where(feedLike.feedId.eq(feed.id)
                    .and(feedLike.userId.eq(currentUserId)))
                .exists()
        );
    }

    /**
     * 피드 개수 조회
     *
     * <p>키워드, 날씨, 작성자 조건에 맞는 전체 피드 수를 카운트한다.
     * 조건이 null이면 무시된다.</p>
     *
     * @param keywordLike       피드 본문에 포함될 키워드
     * @param skyStatus         하늘 상태 필터
     * @param precipitationType 강수 유형 필터
     * @param authorId          작성자 ID 필터
     * @return 조건에 맞는 전체 피드 개수 (조건 없으면 전체 개수)
     */
    @Override
    public long countFeeds(String keywordLike, SkyStatus skyStatus, PrecipitationType precipitationType, UUID authorId) {
        QFeed feed = QFeed.feed;
        QWeather weather = QWeather.weather;

        Long count = queryFactory
            .select(feed.count())
            .from(feed)
            .join(weather).on(feed.weatherId.eq(weather.id))
            .where(
                keywordLike(keywordLike),
                skyStatusEq(skyStatus),
                precipitationEq(precipitationType),
                authorIdEq(authorId)
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanExpression keywordLike(String keyword) {
        return keyword != null ? QFeed.feed.content.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression skyStatusEq(SkyStatus status) {
        return status != null ? QWeather.weather.skyStatus.eq(status) : null;
    }

    private BooleanExpression precipitationEq(PrecipitationType type) {
        return type != null ? QWeather.weather.precipitationType.eq(type) : null;
    }

    private BooleanExpression authorIdEq(UUID authorId) {
        if (authorId == null) {
            return null;
        }
        return QFeed.feed.authorId.eq(authorId);
    }

    /**
     * 커서 기반 페이지네이션 조건
     *
     * <p>정렬 기준(createdAt 또는 likeCount)과 cursor/idAfter 값을 조합해
     * 다음 페이지 조회를 위한 조건식을 만든다.</p>
     *
     * @param request FeedListRequest (cursor, idAfter 포함)
     * @return 커서 조건식, cursor나 idAfter가 null이면 null 반환
     */
    private BooleanExpression cursorAfter(FeedListRequest request) {
        QFeed feed = QFeed.feed;
        if (request.cursor() == null || request.idAfter() == null) {
            return null;
        }
        return switch (request.sortBy()) {
            case "createdAt" -> feed.createdAt.lt(Instant.parse(request.cursor()))
                .or(feed.createdAt.eq(Instant.parse(request.cursor()))
                    .and(feed.id.lt(request.idAfter())));
            case "likeCount" -> feed.likeCount.lt(Long.parseLong(request.cursor()))
                .or(feed.likeCount.eq(Long.parseLong(request.cursor()))
                    .and(feed.id.lt(request.idAfter())));
            default -> throw new IllegalArgumentException("유효하지 않은 sortBy: " + request.sortBy());
        };
    }

    /**
     * 정렬 조건 생성
     *
     * <p>정렬 기준(sortBy)와 정렬 방향(dir)을 기반으로 OrderSpecifier 배열을 만든다.
     * tie-breaker로 항상 feed.id DESC를 추가한다.</p>
     *
     * @param sortBy 정렬 기준 (createdAt, likeCount)
     * @param sortDirection    정렬 방향 (ASCENDING, DESCENDING)
     * @return OrderSpecifier 리스트
     */
    private List<OrderSpecifier<?>> feedSort(String sortBy, SortDirection sortDirection) {
        QFeed feed = QFeed.feed;
        Order order = sortDirection == SortDirection.ASCENDING ? Order.ASC : Order.DESC;

        return switch (sortBy) {
            case "createdAt" -> List.of(
                new OrderSpecifier<>(order, feed.createdAt),
                new OrderSpecifier<>(Order.DESC, feed.id)
            );
            case "likeCount" -> List.of(
                new OrderSpecifier<>(order, feed.likeCount),
                new OrderSpecifier<>(Order.DESC, feed.id)
            );
            default -> throw new IllegalArgumentException("유효하지 않은 sortBy: " + sortBy);
        };
    }
}