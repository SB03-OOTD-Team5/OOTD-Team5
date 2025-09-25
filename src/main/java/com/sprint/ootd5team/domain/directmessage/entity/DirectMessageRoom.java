package com.sprint.ootd5team.domain.directmessage.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tbl_dm_rooms",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_dm_rooms_dm_key", columnNames = "dm_key")
	})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@AllArgsConstructor
@Builder
public class DirectMessageRoom extends BaseEntity {

	@Column(name = "dm_key", length = 80, nullable = false, unique = true)
	private String dmKey;   // UUID1_UUID2 조합

	@Column(name = "user1_id")
	private UUID user1Id;   // 참여자1 (onDelete = null)

	@Column(name = "user2_id")
	private UUID user2Id;   // 참여자2 (onDelete = null)

	// 양방향 매핑 - 해당 채팅방 내 메세지 조회
	@OneToMany(mappedBy = "directMessageRoom", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("createdAt ASC, id ASC")
	@Builder.Default
	private List<DirectMessage> messages = new ArrayList<>();

	// 연관관계 편의 메서드
	public void addMessage(DirectMessage message) {
		messages.add(message);
		message.setDirectMessageRoom(this);
	}
}