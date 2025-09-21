package com.sprint.ootd5team.base.storage;

import com.sprint.ootd5team.base.exception.file.FileDeleteFailedException;
import com.sprint.ootd5team.base.exception.file.FilePermanentSaveFailedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import com.sprint.ootd5team.base.exception.file.FileTooLargeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Slf4j
@Component
@ConditionalOnProperty(name = "ootd.storage.type", havingValue = "s3")
public class S3FileStorage implements FileStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final long maxUploadSize;

    @Value("${ootd.storage.s3.bucket}")
    private String bucket;

    @Value("${ootd.storage.s3.presigned-url-expiration:600}")
    private int presignedUrlExpiration;

    public S3FileStorage(
        S3Client s3Client,
        S3Presigner s3Presigner,
        @Value("${ootd.storage.s3.max-upload-size}") DataSize maxUploadSize
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.maxUploadSize = maxUploadSize.toBytes();
    }

    /**
     * 파일 업로드 (랜덤 UUID prefix 붙여서 key 충돌 방지)
     */
    @Override
    public String upload(String filename, InputStream inputStream, String contentType) {
        String extension = "";
        if (filename != null && filename.contains(".")) {
            extension = filename.substring(filename.lastIndexOf('.'));
        }

        contentType = resolveContentType(filename, extension, contentType);

        String key = "clothes/" + UUID.randomUUID() + extension;
        Path tempFile = null;

        try {
            // 1. 스트림 → temp 파일 생성 (크기 제한 검사 포함)
            final Path tmp = Files.createTempFile("upload-", extension);
            tempFile = tmp; // finally 블록에서 삭제할 수 있도록 저장

            try (var out = Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING)) {
                byte[] buf = new byte[8192];
                long total = 0L;
                int n;
                while ((n = inputStream.read(buf)) != -1) {
                    total += n;
                    if (total > maxUploadSize) {
                        Files.deleteIfExists(tmp);
                        throw FileTooLargeException.withSize(total, maxUploadSize);
                    }
                    out.write(buf, 0, n);
                }
            }

            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

            // 2. putObject만 재시도
            RetryTemplate template = RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(2000, 2.0, 30000)
                .build();

            template.execute(ctx -> {
                s3Client.putObject(request, RequestBody.fromFile(tmp));
                return null;
            }, ctx -> {
                log.error("[S3] 업로드 모든 재시도 실패 - filename={}, cause={}",
                    filename, ctx.getLastThrowable().toString());
                throw FilePermanentSaveFailedException.withFileName(filename);
            });

            log.info("[S3] 업로드 성공: key={}, type={}", key, contentType);
            return key;

        } catch (IOException e) {
            throw FileSaveFailedException.withFileName(filename, e);

        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignore) {
                }
            }
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
                .responseContentDisposition("inline")   // 브라우저 바로 보기
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
            throw FileDeleteFailedException.withFilePath(key, e);
        }
    }

    @Override
    public String resolveUrl(String path) {
        return path != null ? download(path) : null;
    }

    private String resolveContentType(String filename, String extension, String contentType) {
        if (contentType != null && !contentType.equals("application/octet-stream")) {
            return contentType;
        }

        try {
            String probed = Files.probeContentType(Path.of(filename));
            if (probed != null) {
                return probed;
            }
        } catch (IOException ignore) {
        }

        // 확장자 기반 fallback
        if (".png".equalsIgnoreCase(extension)) {
            return "image/png";
        }
        if (".jpg".equalsIgnoreCase(extension) || ".jpeg".equalsIgnoreCase(extension)) {
            return "image/jpeg";
        }
        if (".gif".equalsIgnoreCase(extension)) {
            return "image/gif";
        }
        if (".webp".equalsIgnoreCase(extension)) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

}
