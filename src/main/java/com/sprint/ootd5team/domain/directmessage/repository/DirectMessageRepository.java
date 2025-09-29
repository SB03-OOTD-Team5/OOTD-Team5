package com.sprint.ootd5team.domain.directmessage.repository;

import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

	long countByDirectMessageRoom_Id(UUID roomId);

	// === DESC: 첫 페이지(커서 없음) - 최신부터 ===
	@Query("""
        select m
        from DirectMessage m
        where m.directMessageRoom.id = :roomId
        order by m.createdAt desc, m.id desc
    """)
	List<DirectMessage> firstPageDesc(@Param("roomId") UUID roomId, Pageable pageable);

	// === DESC: createdAt 커서만 있을 때 (해당 시각 '이전' 메시지) ===
	@Query("""
        select m
        from DirectMessage m
        where m.directMessageRoom.id = :roomId
          and m.createdAt < :beforeCreatedAt
        order by m.createdAt desc, m.id desc
    """)
	List<DirectMessage> pageBeforeCreatedAtDesc(@Param("roomId") UUID roomId,
		@Param("beforeCreatedAt") Instant beforeCreatedAt,
		Pageable pageable);

	// === DESC: createdAt + id 커서가 있을 때 (안정 정렬) ===
	@Query("""
        select m
        from DirectMessage m
        where m.directMessageRoom.id = :roomId
          and (
                m.createdAt < :beforeCreatedAt
             or (m.createdAt = :beforeCreatedAt and m.id < :beforeId)
          )
        order by m.createdAt desc, m.id desc
    """)
	List<DirectMessage> pageBeforeDesc(@Param("roomId") UUID roomId,
		@Param("beforeCreatedAt") Instant beforeCreatedAt,
		@Param("beforeId") UUID beforeId,
		Pageable pageable);
}