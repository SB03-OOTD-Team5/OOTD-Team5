package com.sprint.ootd5team.domain.profile.repository;

import com.sprint.ootd5team.domain.profile.entity.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findById(UUID id);

    Optional<Profile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @EntityGraph(attributePaths = "location")
    List<Profile> findAllByLocationIsNotNull();
}