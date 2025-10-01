package com.sprint.ootd5team.domain.directmessage.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tbl_dm_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@AllArgsConstructor
@Builder
public class DirectMessage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "room_id", nullable = false)
	private DirectMessageRoom directMessageRoom;   // FK로 채팅방 참조

	@Column(name = "sender_id", nullable = false)
	private UUID senderId;   // 메세지 송신자 (onDelete = null)

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;  // 메시지 본문
}