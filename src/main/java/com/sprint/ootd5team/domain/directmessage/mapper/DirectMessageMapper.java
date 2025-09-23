package com.sprint.ootd5team.domain.directmessage.mapper;

import com.sprint.ootd5team.domain.directmessage.dto.DirectMessageDto;
import com.sprint.ootd5team.domain.directmessage.entity.DirectMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DirectMessageMapper {

	@Mapping(target = "sender", ignore = true)   // 서비스에서 UserRepository 통해 보강
	@Mapping(target = "receiver", ignore = true)   // 서비스에서 ChatRoom 기반으로 계산
	DirectMessageDto toDto(DirectMessage directMessage);
}
