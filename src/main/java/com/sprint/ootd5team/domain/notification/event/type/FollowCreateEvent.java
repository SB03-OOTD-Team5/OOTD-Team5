//package com.sprint.ootd5team.domain.notification.event.type;
//
//import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
//import java.util.List;
//import java.util.UUID;
//
///**
// * FOLLOWED("%님이 나를 팔로우 했어요", "%s"),
// * 팔로우를 요청한 사람 이름(follower)
// *
// * receiverId = 팔로우를 요청 받은 사람 이름(followee)
// */
//public class FollowCreateEvent extends DomainEvent<FollowDto>{
//
//    public FollowCreateEvent(FollowDto data) {
//        super(data);
//    }
//
//    @Override
//    public NotificationTemplateType getTemplateType() {
//        return NotificationTemplateType.FOLLOWED;
//    }
//
//    @Override
//    public Object[] getArgs() {
//        return new Object[]{getData().follower().name()};
//    }
//
//    @Override
//    public List<UUID> getReceiverIds() {
//        return List.of(getData().followee().userId());
//    }
//}
