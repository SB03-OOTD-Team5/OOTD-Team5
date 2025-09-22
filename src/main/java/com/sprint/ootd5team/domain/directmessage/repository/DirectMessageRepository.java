package com.sprint.ootd5team.domain.directmessage.repository;

import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

	// 특정 방의 전체 메시지 조회 (오래된 순)
	List<DirectMessage> findAllByDirectMessageRoom_IdOrderByCreatedAtAsc(UUID roomId);

	// 특정 방의 최근 메시지 N개 조회 (최신순)
	List<DirectMessage> findTop20ByDirectMessageRoom_IdOrderByCreatedAtDesc(UUID roomId);
}
