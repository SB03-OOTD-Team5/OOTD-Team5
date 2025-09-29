package com.sprint.ootd5team.domain.follow.repository;

import com.sprint.ootd5team.domain.follow.entity.Follow;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowRepositoryCustom {

    @Query("select f.followerId from Follow f where f.followeeId = :authorId")
    List<UUID> findFollowerIds(@Param("authorId") UUID authorId);
}