package com.sprint.ootd5team.domain.directmessage.service;

import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessageRoom;
import com.sprint.ootd5team.domain.directmessage.mapper.DirectMessageMapper;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRepository;
import com.sprint.ootd5team.domain.directmessage.repository.DirectMessageRoomRepository;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

	private final DirectMessageRepository messageRepository;
	private final DirectMessageRoomRepository roomRepository;
	private final UserRepository userRepository;
	private final DirectMessageMapper messageMapper;

	/**
	 * 메시지 전송
	 */
	@Transactional
	public DirectMessageDto sendMessage(UUID roomId, UUID senderId, String content) {
		DirectMessageRoom room = roomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

		DirectMessage message = DirectMessage.builder()
			.directMessageRoom(room)
			.senderId(senderId)
			.content(content)
			.build();

		DirectMessage saved = messageRepository.save(message);

		return enrichDto(messageMapper.toDto(saved), room, senderId);
	}

	/**
	 * 특정 방의 메시지 전체 조회
	 */
	public List<DirectMessageDto> getMessages(UUID roomId) {
		DirectMessageRoom room = roomRepository.findById(roomId)
			.orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

		return messageRepository.findAllByDirectMessageRoom_IdOrderByCreatedAtAsc(roomId)

			.stream()
			.map(dm -> enrichDto(messageMapper.toDto(dm), room, dm.getSenderId()))
			.toList();
	}

	/**
	 * DTO 보강: senderName, receiverId, receiverName
	 */
	private DirectMessageDto enrichDto(DirectMessageDto dto, DirectMessageRoom room, UUID senderId) {
		// sender
		String senderName = userRepository.findById(senderId)
			.map(User::getName)
			.orElse("탈퇴한 사용자");

		// receiver
		UUID receiverId = room.getUser1Id().equals(senderId)
			? room.getUser2Id()
			: room.getUser1Id();

		String receiverName = userRepository.findById(receiverId)
			.map(User::getName)
			.orElse("탈퇴한 사용자");

		return DirectMessageDto.builder()
			.id(dto.id())
			.createdAt(dto.createdAt())
			.senderId(dto.senderId())
			.senderName(senderName)
			.receiverId(receiverId)
			.receiverName(receiverName)
			.content(dto.content())
			.build();
	}
}
