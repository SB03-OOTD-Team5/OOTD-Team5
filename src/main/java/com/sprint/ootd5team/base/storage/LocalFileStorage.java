package com.sprint.ootd5team.base.storage;

import com.sprint.ootd5team.base.exception.file.FileDeleteFailedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ootd.storage.type", havingValue = "local")
public class LocalFileStorage implements FileStorage {

    private final Path root;

    // 기본 생성자 "uploads" 폴더 사용
    public LocalFileStorage() {
        this(Paths.get("uploads"));
    }

    // 테스트/특수 환경에서는 root를 직접 지정 가능
    public LocalFileStorage(Path root) {
        this.root = root;
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("업로드 폴더 생성 실패", e);
        }
    }

    @Override
    public String upload(String filename, InputStream input, String contentType) {
        try {
            Path target = root.resolve(filename);
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw FileSaveFailedException.withFileName(filename);
        }
    }

    @Override
    public String download(String path) {
        return Paths.get(path).toAbsolutePath().toUri().toString();
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            throw new FileDeleteFailedException(path);
        }
    }

    @Override
    public String resolveUrl(String path) {
        return path != null ? "/local-files/" + path : null;
    }
}
