package com.sprint.ootd5team.base.storage;

import com.sprint.ootd5team.base.exception.file.FileDeleteFailedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
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
        String ext = "";
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex != -1) {
            ext = filename.substring(dotIndex);
        }
        String uniqueName = "clothes/" + UUID.randomUUID() + ext;

        try {
            Path target = root.resolve(uniqueName).normalize();
            if (!target.startsWith(root)) {
                throw new SecurityException("Invalid path");
            }

            Files.createDirectories(target.getParent());
            try (InputStream in = input) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueName;
        } catch (IOException e) {
            throw FileSaveFailedException.withFileName(uniqueName, e);
        }
    }

    @Override
    public String download(String path) {
        Path p = root.resolve(path).normalize();
        if (!p.startsWith(root)) {
            throw new SecurityException("Invalid path");
        }
        return p.toAbsolutePath().toUri().toString();
    }

    @Override
    public void delete(String path) {
        try {
            Path p = root.resolve(path).normalize();
            if (!p.startsWith(root)) {
                throw new SecurityException("Invalid path");
            }
            Files.deleteIfExists(p);
        } catch (IOException e) {
            throw FileDeleteFailedException.withFilePath(path, e);
        }
    }

    @Override
    public String resolveUrl(String path) {
        return path != null ? "/local-files/" + path : null;
    }
}
