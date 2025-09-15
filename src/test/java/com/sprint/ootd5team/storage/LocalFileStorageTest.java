package com.sprint.ootd5team.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.storage.LocalFileStorage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocalFileStorageTest {

    private LocalFileStorage localFileStorage;

    @AfterEach
    void cleanup() throws IOException {
        Files.walk(Paths.get("uploads"))
            .filter(Files::isRegularFile)
            .forEach(path -> path.toFile().delete());
    }

    @BeforeEach
    void setUp() {
        localFileStorage = new LocalFileStorage();
    }

    @Test
    void 업로드_성공() throws Exception {
        String filename = "test.txt";
        byte[] content = "hello world".getBytes();
        InputStream input = new ByteArrayInputStream(content);

        String savedPath = localFileStorage.upload(filename, input);
        System.out.println("savedPath = " + savedPath);
        Thread.sleep(5000); // 5초 동안 파일 확인 가능

        File savedFile = new File(savedPath);
        assertThat(savedFile).exists();
    }

    @Test
    void 다운로드_URL_반환() {
        // given
        String filename = "test-download.txt";
        InputStream input = new ByteArrayInputStream("download test".getBytes());
        String savedPath = localFileStorage.upload(filename, input);

        // when
        String url = localFileStorage.download(savedPath);

        // then
        assertThat(url).startsWith("file:/");
        assertThat(url).contains("uploads");
        System.out.println("url = " + url);
    }

    @Test
    void 삭제_성공() {
        // given
        String filename = "test-delete.txt";
        InputStream input = new ByteArrayInputStream("to be deleted".getBytes());
        String savedPath = localFileStorage.upload(filename, input);

        // when
        localFileStorage.delete(savedPath);

        // then
        assertThat(new File(savedPath)).doesNotExist();
    }
}
