package com.sprint.ootd5team.domain.directmessage.repository;

import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {
}
