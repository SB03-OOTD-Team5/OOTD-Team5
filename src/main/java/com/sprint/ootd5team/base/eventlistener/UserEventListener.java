package com.sprint.ootd5team.base.eventlistener;

import com.sprint.ootd5team.domain.user.dto.TemporaryPasswordCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

    @Value("${ootd.email.sender}")
    private String sender;

    private final JavaMailSender mailSender;

    @Async("eventTaskExecutor")
    @TransactionalEventListener
    public void on(TemporaryPasswordCreatedEvent event){
        log.info("임시 비밀번호 메일 발송 이벤트 시작 email:{}",event.email());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(event.email());
        message.setSubject("임시 비밀번호 발급 - OTBOO");
        // 본문 구성
        String body =
            "OTBOO\n"
                + "임시 비밀번호가 발급되었습니다\n\n"
                + "안녕하세요, " + event.name() + "님!\n\n"
                + "요청하신 임시 비밀번호가 발급되었습니다. "
                + "아래 임시 비밀번호를 사용하여 로그인 후 새로운 비밀번호로 변경해주세요.\n\n"
                + "임시 비밀번호\n"
                + event.tempPassword() + "\n\n"
                + "⚠️ 중요 안내사항\n"
                + "• 이 임시 비밀번호는 " + event.expireAt() + " 까지만 유효합니다\n"
                + "• 보안을 위해 로그인 후 즉시 새로운 비밀번호로 변경해주세요\n"
                + "• 임시 비밀번호는 다른 사람과 공유하지 마세요\n\n"
                + "본 메일은 발신전용이므로 회신되지 않습니다.\n"
                + "문의사항이 있으시면 고객센터로 연락해주세요.";

        message.setText(body);
        mailSender.send(message);


        log.info("임시 비밀번호 메일 발송 이벤트 완료 email:{}",event.email());
    }
}
