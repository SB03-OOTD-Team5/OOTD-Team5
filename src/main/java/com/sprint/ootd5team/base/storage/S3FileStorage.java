package com.sprint.ootd5team.base.storage;

import com.sprint.ootd5team.base.exception.file.FilePermanentSaveFailedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ootd.storage.type", havingValue = "s3")
public class S3FileStorage implements FileStorage{

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${ootd.storage.s3.bucket}")
    private String bucket;

    @Value("${ootd.storage.s3.presigned-url-expiration:600}")
    private int presignedUrlExpiration;

    /**
     * 파일 업로드 (랜덤 UUID prefix 붙여서 key 충돌 방지)
     */
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Override
    public String upload(String filename, InputStream inputStream) {
        String key = "clothes/" + UUID.randomUUID() + "_" + filename;
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            byte[] bytes = inputStream.readAllBytes();
            s3Client.putObject(request, RequestBody.fromBytes(bytes));

            log.info("[S3] 업로드 성공: key={}, size={}", key, bytes.length);
            return key;
        } catch (Exception e) {
            log.error("[S3] 업로드 실패: file={}, ex={}", filename, e.toString(), e);
            throw FileSaveFailedException.withFileName(key);
        }
    }

    /**
     * Presigned URL 다운로드
     */
    @Override
    public String download(String key) {
        try {
            var getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            var presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                .getObjectRequest(getReq)
                .build();

            String url = s3Presigner.presignGetObject(presignReq).url().toString();
            log.info("[S3] Presigned URL 생성: key={}, url={}", key, url);

            return url;
        } catch (NoSuchKeyException e) {
            log.warn("[S3] 다운로드 실패 - 존재하지 않는 key={}", key);
            throw e;
        }
    }

    /**
     * Controller에서 302 리다이렉트 응답으로 Presigned URL 내려줄 때 사용
     */
    public ResponseEntity<Resource> redirectToPresignedUrl(String key) {
        String presignedUrl = download(key);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(presignedUrl))
            .build();
    }

    /**
     * S3 파일 삭제
     */
    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(b -> b.bucket(bucket).key(key));
            log.info("[S3] 삭제 성공: key={}", key);
        } catch (Exception e) {
            log.error("[S3] 삭제 실패: key={}, ex={}", key, e.toString(), e);
        }
    }

    /**
     * 업로드 재시도 실패 시 복구 처리
     */
    @Recover
    public String recover(Exception e, String filename, InputStream inputStream) {
        String requestId = MDC.get("requestId");
        log.error("[S3] 업로드 모든 재시도 실패 - filename={}, requestId={}, cause={}",
            filename, requestId, e.toString(), e);

        throw FilePermanentSaveFailedException.withFileName(filename);
    }

}
