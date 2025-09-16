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

    private final Path root = Paths.get("uploads");

    public LocalFileStorage() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("업로드 폴더 생성 실패", e);
        }
    }

    @Override
    public String upload(String filename, InputStream input) {
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
}
