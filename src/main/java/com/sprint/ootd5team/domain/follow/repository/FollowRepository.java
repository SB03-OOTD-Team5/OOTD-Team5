package com.sprint.ootd5team.domain.follow.repository;

import com.sprint.ootd5team.domain.follow.entity.Follow;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowRepositoryCustom {

    @Query("""
    SELECT f.followerId
    FROM Follow f
    WHERE f.followeeId = :followeeId
      AND f.followerId <> :followeeId
""")
    List<UUID> findFollowerIds(@Param("followeeId") UUID followeeId);
}