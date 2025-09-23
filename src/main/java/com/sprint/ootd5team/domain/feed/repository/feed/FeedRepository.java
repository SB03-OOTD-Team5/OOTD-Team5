package com.sprint.ootd5team.domain.feed.repository.feed;

import com.sprint.ootd5team.domain.feed.entity.Feed;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedRepositoryCustom {

    @Modifying
    @Query("update Feed f set f.likeCount = f.likeCount + 1 where f.id = :feedId")
    void incrementLikeCount(@Param("feedId") UUID feedId);

    @Modifying
    @Query("update Feed f set f.likeCount = f.likeCount - 1 where f.id = :feedId and f.likeCount > 0")
    int decrementLikeCount(@Param("feedId") UUID feedId);
}