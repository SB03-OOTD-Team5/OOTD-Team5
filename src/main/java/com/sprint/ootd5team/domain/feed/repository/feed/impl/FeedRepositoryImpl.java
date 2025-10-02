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
import com.sprint.ootd5team.domain.feed.dto.data.SortSpecDto;
import com.sprint.ootd5team.domain.feed.dto.enums.SortDirection;
import com.sprint.ootd5team.domain.feed.dto.request.FeedListRequest;
import com.sprint.ootd5team.domain.feed.entity.QFeed;
import com.sprint.ootd5team.base.exception.feed.InvalidSortOptionException;
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

        SortSpecDto sortSpec = buildSortSpec(request);

        return queryFactory
            .select(feedProjection(currentUserId))
            .from(feed)
            .join(user).on(feed.authorId.eq(user.id))
            .leftJoin(profile).on(profile.user.id.eq(user.id))
            .join(weather).on(feed.weatherId.eq(weather.id))
            .where(
                keywordLike(request.keywordLike()),
                skyStatusEq(request.skyStatusEqual()),
                precipitationEq(request.precipitationTypeEqual()),
                authorIdEq(request.authorIdEqual()),
                sortSpec.cursorCondition()
            )
            .orderBy(sortSpec.orderSpecifiers().toArray(OrderSpecifier[]::new))
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
            .leftJoin(profile).on(profile.user.id.eq(user.id))
            .join(weather).on(feed.weatherId.eq(weather.id))
            .where(feed.id.eq(feedId))
            .fetchOne();
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
                profile.name,
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
     * 정렬 조건과 커서 조건을 한 번에 생성
     */
    private SortSpecDto buildSortSpec(FeedListRequest request) {
        QFeed feed = QFeed.feed;
        boolean asc = request.sortDirection() == SortDirection.ASCENDING;
        String cursor = request.cursor();
        UUID idAfter = request.idAfter();

        return switch (request.sortBy()) {
            case "createdAt" -> {
                List<OrderSpecifier<?>> orders = List.of(
                    new OrderSpecifier<>(asc ? Order.ASC : Order.DESC, feed.createdAt),
                    new OrderSpecifier<>(Order.ASC, feed.id)
                );
                BooleanExpression cursorCondition = (cursor != null && idAfter != null)
                    ? (asc
                    ? feed.createdAt.gt(Instant.parse(cursor))
                    .or(feed.createdAt.eq(Instant.parse(cursor)).and(feed.id.gt(idAfter)))
                    : feed.createdAt.lt(Instant.parse(cursor))
                        .or(feed.createdAt.eq(Instant.parse(cursor)).and(feed.id.lt(idAfter))))
                    : null;
                yield new SortSpecDto(orders, cursorCondition);
            }
            case "likeCount" -> {
                List<OrderSpecifier<?>> orders = List.of(
                    new OrderSpecifier<>(asc ? Order.ASC : Order.DESC, feed.likeCount),
                    new OrderSpecifier<>(Order.ASC, feed.id)
                );
                BooleanExpression cursorCondition = (cursor != null && idAfter != null)
                    ? (asc
                    ? feed.likeCount.gt(Long.parseLong(cursor))
                    .or(feed.likeCount.eq(Long.parseLong(cursor)).and(feed.id.gt(idAfter)))
                    : feed.likeCount.lt(Long.parseLong(cursor))
                        .or(feed.likeCount.eq(Long.parseLong(cursor)).and(feed.id.lt(idAfter))))
                    : null;
                yield new SortSpecDto(orders, cursorCondition);
            }
            default -> throw InvalidSortOptionException.withSortBy(request.sortBy());
        };
    }
}