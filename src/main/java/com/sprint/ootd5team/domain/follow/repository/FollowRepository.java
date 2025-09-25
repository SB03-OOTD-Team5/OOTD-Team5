package com.sprint.ootd5team.domain.follow.repository;

import com.sprint.ootd5team.domain.follow.entity.Follow;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID>, FollowRepositoryCustom {

}