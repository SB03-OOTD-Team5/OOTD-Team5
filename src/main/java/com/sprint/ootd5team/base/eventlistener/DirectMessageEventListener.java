package com.sprint.ootd5team.base.eventlistener;

import com.sprint.ootd5team.domain.directmessage.dto.event.DirectMessageCommittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectMessageEventListener {

	private final SimpMessagingTemplate messagingTemplate;

	// 트랜잭션이 성공적으로 커밋된 뒤 호출
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onCommitted(DirectMessageCommittedEvent event) {
		messagingTemplate.convertAndSend(event.destination(), event.payload());
		log.debug("[DM EventListener] 구독자에게 브로드캐스트: destination={}", event.destination());
	}
}