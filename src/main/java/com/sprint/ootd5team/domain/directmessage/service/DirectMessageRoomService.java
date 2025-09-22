package com.sprint.ootd5team.domain.directmessage.service;

import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageRoomService {

	private final DirectMessageRoomRepository roomRepository;

	/**
	 * dmKey로 방 조회
	 */
	public Optional<DirectMessageRoom> findByDmKey(String dmKey) {
		return roomRepository.findByDmKey(dmKey);
	}

	/**
	 * 채팅방 생성
	 */
	@Transactional
	public DirectMessageRoom createRoom(UUID user1Id, UUID user2Id) {
		String dmKey = user1Id.compareTo(user2Id) < 0
			? user1Id + "_" + user2Id
			: user2Id + "_" + user1Id;

		return roomRepository.findByDmKey(dmKey)
			.orElseGet(() -> roomRepository.save(
				DirectMessageRoom.builder()
					.dmKey(dmKey)
					.user1Id(user1Id)
					.user2Id(user2Id)
					.build()
			));
	}
}
