package com.sprint.ootd5team.domain.profile.repository;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findById(UUID id);

    Optional<Profile> findByUserId(UUID userId);

}
