package com.sprint.ootd5team.base.eventlistener;


import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.profile.event.ImageFileUploadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileEventListener {

    private final FileStorage fileStorage;

    @Async("imageUploadExecutor")
    // 기존 메서드를 COMMIT 후 이벤트 실행(해당 이벤트 실행완료 전에는 기존 이미지 파일 유지)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ImageFileUploadEvent event)  {
        String previousImageUrl = event.previousImageUrl();
        // DB 저장 성공 후 이전 파일 삭제
        if (previousImageUrl != null) {
            fileStorage.delete(previousImageUrl);
        }
    }
}
