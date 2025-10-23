package com.sprint.ootd5team.domain.notification.event.type.single;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprint.ootd5team.domain.follow.dto.data.FollowDto;
import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
import com.sprint.ootd5team.domain.notification.event.type.base.SingleReceiverEvent;
import java.util.UUID;

/**
 * FOLLOWED("%님이 나를 팔로우 했어요", "%s"),
 * 팔로우를 요청한 사람 이름(follower)
 * <p>
 * receiverId = 팔로우를 요청 받은 사람 (followee)
 * <p>
 * eventPublisher.publishEvent(
 * new FollowCreatedEvent(FollowDto))
 * );
 */
@JsonTypeName("follow-created")
public class FollowCreatedEvent extends SingleReceiverEvent<FollowDto> {

    @JsonCreator
    public FollowCreatedEvent(
        @JsonProperty("data") FollowDto data,
        @JsonProperty("receiver") UUID receiver
    ) {
        super(data, receiver);
    }

    public FollowCreatedEvent(FollowDto data) {
        super(data, data.followee().userId());
    }

    @Override
    public NotificationTemplateType getTemplateType() {
        return NotificationTemplateType.FOLLOWED;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{getData().follower().name()};
    }
}
