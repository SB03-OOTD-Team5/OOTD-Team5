package com.sprint.ootd5team.domain.notification.mapper;

import com.sprint.ootd5team.domain.notification.dto.response.NotificationDto;
import com.sprint.ootd5team.domain.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "receiver.id", target = "receiverId")
    NotificationDto toDto(Notification entity);

}
