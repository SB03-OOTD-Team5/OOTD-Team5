package com.sprint.ootd5team.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.storage.LocalFileStorage;
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
    private static final String TEST_IMAGE = "/test-image.png";

    /**
     * 각 테스트 종료 후 업로드된 파일 삭제.
     * 파일을 직접 확인하려면 이 메서드를 주석 처리한다.
     */
    @AfterEach
    void cleanup() throws IOException {
        if (Files.exists(Paths.get("uploads"))) {
            Files.walk(Paths.get("uploads"))
                .filter(Files::isRegularFile)
                .forEach(path -> path.toFile().delete());
        }
    }

    @BeforeEach
    void setUp() {
        localFileStorage = new LocalFileStorage();
    }

    @Test
    void 업로드_성공() throws Exception {
        // given
        String filename = "test-image.png";
        try (InputStream input = getClass().getResourceAsStream("/test-image.png")) {
            assertThat(input).isNotNull();

            // when
            String savedPath = localFileStorage.upload(filename, input, "image/png");
            System.out.println("savedPath = " + savedPath);

            // then
            File savedFile = new File(savedPath);
            assertThat(savedFile).exists();
            assertThat(Files.probeContentType(savedFile.toPath())).isEqualTo("image/png");
        }
    }

    @Test
    void 다운로드_URL_반환() throws Exception {
        // given
        String filename = "download-image.png";
        String savedPath;
        try (InputStream input = getClass().getResourceAsStream(TEST_IMAGE)) {
            assertThat(input).isNotNull();
            savedPath = localFileStorage.upload(filename, input, "image/png");
        }

        // when
        String url = localFileStorage.download(savedPath);

        // then
        assertThat(url).startsWith("file:/");
        assertThat(url).contains("uploads");
        System.out.println("url = " + url);
    }

    @Test
    void 삭제_성공() throws Exception {
        // given
        String filename = "delete-image.png";
        String savedPath;
        try (InputStream input = getClass().getResourceAsStream(TEST_IMAGE)) {
            assertThat(input).isNotNull();
            savedPath = localFileStorage.upload(filename, input, "image/png");
        }

        // when
        localFileStorage.delete(savedPath);

        // then
        assertThat(new File(savedPath)).doesNotExist();
    }
}
