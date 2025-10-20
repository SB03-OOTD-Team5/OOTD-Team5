package com.sprint.ootd5team.domain.comment.repository;

import com.sprint.ootd5team.domain.comment.entity.FeedComment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedCommentRepository extends JpaRepository<FeedComment, UUID>, FeedCommentRepositoryCustom {

    long countByFeedId(UUID feedId);
}