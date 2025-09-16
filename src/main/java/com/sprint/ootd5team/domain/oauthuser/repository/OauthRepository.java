package com.sprint.ootd5team.domain.oauthuser.repository;

import com.sprint.ootd5team.domain.oauthuser.entity.OauthUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthRepository extends JpaRepository<OauthUser, UUID> {


    List<OauthUser> findByUserId(UUID userId);
}
