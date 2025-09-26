package com.sprint.ootd5team.domain.user.repository;

import com.sprint.ootd5team.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select u.id from User u")
    List<UUID> findAllUserIds();

    @Query("select u.name from User u where u.id = :id")
    String findUserNameById(@Param("id") UUID id);
}
