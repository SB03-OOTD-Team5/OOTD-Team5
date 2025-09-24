//package com.sprint.ootd5team.domain.notification.event.type;
//
//import com.sprint.ootd5team.domain.notification.enums.NotificationTemplateType;
//import java.util.List;
//import java.util.UUID;
//
///**
// * DM_RECEIVED("[DM] %s", "%s"),
// * 메시지 발신자(sender) 이름, 메시지 내용
// *
// * receiverId = 수신자 id
// */
//public class DmCreateEvent extends DomainEvent<DirectMessageDto>{
//    public DmCreateEvent(DirectMessageDto data) {
//        super(data);
//    }
//
//    @Override
//    public NotificationTemplateType getTemplateType() {
//        return NotificationTemplateType.FEED_FOLLOW_CREATED;
//    }
//
//    @Override
//    public Object[] getArgs() {
//        return new Object[]{getData().sender().name(), getData().content};
//    }
//
//    @Override
//    public List<UUID> getReceiverIds() {
//        return List.of(getData().receiver().userId());
//    }
//}
