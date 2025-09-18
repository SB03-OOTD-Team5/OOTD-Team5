package com.sprint.ootd5team.domain.profile.repository;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    boolean existsByUserId(UUID userId);
}
