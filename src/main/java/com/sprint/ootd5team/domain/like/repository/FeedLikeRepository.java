package com.sprint.ootd5team.domain.like.repository;

import com.sprint.ootd5team.domain.like.entity.FeedLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

    boolean existsByFeedIdAndUserId(UUID feedId, UUID userId);

    void deleteByFeedIdAndUserId(UUID feedId, UUID userId);
}
