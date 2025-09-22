package com.sprint.ootd5team.domain.directmessage.repository;

import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface DirectMessageRoomRepository extends CrudRepository<DirectMessageRoom, UUID> {

	// dmKey로 방을 조회
	Optional<DirectMessageRoom> findByDmKey(String dmKey);
}