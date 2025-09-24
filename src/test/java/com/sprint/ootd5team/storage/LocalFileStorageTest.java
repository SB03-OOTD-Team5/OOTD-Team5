package com.sprint.ootd5team.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.ootd5team.base.exception.file.FileDeleteFailedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import com.sprint.ootd5team.base.storage.LocalFileStorage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocalFileStorageTest {

    private LocalFileStorage localFileStorage;
    private static final String TEST_IMAGE = "/test-image.png";
    private static final String TEST_PREFIX = "prefix/";
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
            String uniqueName = localFileStorage.upload(filename, input, "image/png", TEST_PREFIX);

            // then
            Path savedFile = Paths.get("uploads").resolve(uniqueName);
            assertThat(savedFile).exists();
            assertThat(Files.probeContentType(savedFile)).isEqualTo("image/png");
        }
    }

    @Test
    void 다운로드_URL_반환() throws Exception {
        // given
        String filename = "download-image.png";
        String savedPath;
        try (InputStream input = getClass().getResourceAsStream(TEST_IMAGE)) {
            assertThat(input).isNotNull();
            savedPath = localFileStorage.upload(filename, input, "image/png", TEST_PREFIX);
        }

        // when
        String url = localFileStorage.download(savedPath);

        // then
        assertThat(url).startsWith("file:");
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
            savedPath = localFileStorage.upload(filename, input, "image/png", TEST_PREFIX);
        }

        // when
        localFileStorage.delete(savedPath);

        // then
        assertThat(new File(savedPath)).doesNotExist();
    }

    @Test
    void 업로드_실패_IO예외() {
        // given
        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("읽기 실패");
            }
        };

        // when & then
        assertThatThrownBy(() -> localFileStorage.upload("broken.png", brokenStream, "image/png", TEST_PREFIX))
            .isInstanceOf(FileSaveFailedException.class);
    }

    @Test
    void 다운로드_실패_루트밖의_경로() {
        // given
        String invalidPath = "../outside.png";

        // when & then
        assertThatThrownBy(() -> localFileStorage.download(invalidPath))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    void 삭제_실패_루트밖의_경로() {
        // given
        String invalidPath = "../outside.png";

        // when & then
        assertThatThrownBy(() -> localFileStorage.delete(invalidPath))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    void 업로드실패_InputStream읽기예외() {
        // given: 항상 IOException 던지는 InputStream
        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("읽기 실패");
            }
        };

        // when & then
        assertThatThrownBy(() ->
            localFileStorage.upload("broken.png", brokenStream, "image/png", TEST_PREFIX)
        )
            .isInstanceOf(FileSaveFailedException.class);
    }


    @Test
    void 삭제실패_FileDelete예외() throws Exception {
        // given
        Path root = Files.createTempDirectory("deletefail");
        LocalFileStorage storage = new LocalFileStorage(root);
        Path fakeFile = root.resolve("locked.txt");
        Files.writeString(fakeFile, "lock");
        fakeFile.toFile().setWritable(false); // 삭제 권한 제한

        // when & then
        assertThatThrownBy(() -> storage.delete("locked.txt"))
            .isInstanceOf(FileDeleteFailedException.class);
    }

    @Test
    void resolveUrl_nullPath() {
        // given
        String result = localFileStorage.resolveUrl(null);

        // then
        assertThat(result).isNull();
    }
}
