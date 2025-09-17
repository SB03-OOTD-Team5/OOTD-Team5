package com.sprint.ootd5team.base.eventlistener;

import com.sprint.ootd5team.domain.user.dto.TemporaryPasswordCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

    private final JavaMailSender mailSender;

    @Async("eventTaskExecutor")
    @TransactionalEventListener
    public void on(TemporaryPasswordCreatedEvent event){
        log.info("임시 비밀번호 메일 발송 이벤트 시작 email:{}",event.email());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sprtms5335@gmail.com");
        message.setTo(event.email());
        message.setSubject("[OOTD] 임시 비밀번호 안내");
        message.setText("안녕하세요, " + event.name() + "님.\n\n"
            + "임시 비밀번호는 아래와 같습니다:\n\n"
            + event.tempPassword() + "\n\n"
            + "해당 비밀번호는 3분 동안만 유효합니다.\n"
            + "로그인 후 반드시 새 비밀번호로 변경해주세요.");
        mailSender.send(message);


        log.info("임시 비밀번호 메일 발송 이벤트 완료 email:{}",event.email());
    }
}
