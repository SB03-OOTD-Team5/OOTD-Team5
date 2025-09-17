package com.sprint.ootd5team.domain.feed.repository.feed;

import com.sprint.ootd5team.domain.feed.entity.Feed;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, UUID>, FeedRepositoryCustom {
}