package com.sprint.ootd5team.domain.feed.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.feed.event.handler.FeedFailedEventHandler;
import com.sprint.ootd5team.domain.feed.event.type.FeedContentUpdatedEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedFailedEventHandler 단위 테스트")
@ActiveProfiles("test")
public class FeedFailedEventHandlerTest {

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private FeedFailedEventHandler handler;

    @TempDir
    Path tempDir;

    private String primaryPath;
    private String backupPath;
    private String topic;
    private FeedContentUpdatedEvent testEvent;
    private Exception testError;

    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        ReflectionTestUtils.setField(handler, "objectMapper", objectMapper);

        primaryPath = tempDir.resolve("primary").toString();
        backupPath = tempDir.resolve("backup").toString();

        ReflectionTestUtils.setField(handler, "primaryPath", primaryPath);
        ReflectionTestUtils.setField(handler, "backupPath", backupPath);
        ReflectionTestUtils.setField(handler, "maxMemoryQueueSize", 1000);

        topic = "ootd.Feeds.ContentUpdated";
        testEvent = new FeedContentUpdatedEvent(UUID.randomUUID(), "테스트 내용");
        testError = new RuntimeException("Kafka 발행 실패");

        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

    @Test
    @DisplayName("Primary path에 실패 이벤트 파일 저장")
    void saveFailedEvent_savesToPrimaryPath() throws IOException {
        // when
        handler.saveFailedEvent(topic, testEvent, testError);

        // then
        Path primaryDir = Path.of(primaryPath);
        assertThat(Files.exists(primaryDir)).isTrue();
        assertThat(Files.isDirectory(primaryDir)).isTrue();

        long fileCount = Files.list(primaryDir)
            .filter(path -> path.toString().endsWith(".json"))
            .count();

        assertThat(fileCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Primary path 실패 시 backup path에 파일 저장")
    void saveFailedEvent_fallsBackToBackupPath() throws IOException {
        // given
        Path fileAsDir = tempDir.resolve("this-is-a-file-not-directory");
        Files.writeString(fileAsDir, "dummy");

        ReflectionTestUtils.setField(handler, "primaryPath", fileAsDir.toString());

        // when
        handler.saveFailedEvent(topic, testEvent, testError);

        // then
        Path backupDir = Path.of(backupPath);
        assertThat(Files.exists(backupDir)).isTrue();

        long fileCount = Files.list(backupDir)
            .filter(path -> path.toString().endsWith(".json"))
            .count();

        assertThat(fileCount).isEqualTo(1);
    }

    @Test
    @DisplayName("파일 시스템 실패 시 메모리 큐에 저장")
    void saveFailedEvent_fallsBackToMemoryQueue() throws IOException {
        // given
        Path fileAsPrimary = tempDir.resolve("file-as-primary");
        Path fileAsBackup = tempDir.resolve("file-as-backup");
        Files.writeString(fileAsPrimary, "dummy");
        Files.writeString(fileAsBackup, "dummy");

        ReflectionTestUtils.setField(handler, "primaryPath", fileAsPrimary.toString());
        ReflectionTestUtils.setField(handler, "backupPath", fileAsBackup.toString());

        // when
        handler.saveFailedEvent(topic, testEvent, testError);

        // then
        var memoryQueue = ReflectionTestUtils.getField(handler, "memoryQueue");
        assertThat(memoryQueue).isNotNull();
        assertThat(((java.util.Queue<?>) memoryQueue).size()).isEqualTo(1);
    }

    @Test
    @DisplayName("메모리 큐가 가득 차면 stderr로 출력")
    void saveFailedEvent_fallsBackToStderr_whenMemoryQueueFull() throws IOException {
        // given
        ReflectionTestUtils.setField(handler, "maxMemoryQueueSize", 0);

        Path fileAsPrimary = tempDir.resolve("file-as-primary2");
        Path fileAsBackup = tempDir.resolve("file-as-backup2");
        Files.writeString(fileAsPrimary, "dummy");
        Files.writeString(fileAsBackup, "dummy");

        ReflectionTestUtils.setField(handler, "primaryPath", fileAsPrimary.toString());
        ReflectionTestUtils.setField(handler, "backupPath", fileAsBackup.toString());

        // when
        handler.saveFailedEvent(topic, testEvent, testError);

        // then
        String output = errContent.toString();
        assertThat(output).contains("KAFKA_FAILURE");
        assertThat(output).contains(topic);
    }

    @Test
    @DisplayName("저장된 파일에 올바른 정보 포함")
    void saveFailedEvent_fileContainsCorrectInformation() throws IOException {
        // when
        handler.saveFailedEvent(topic, testEvent, testError);

        // then
        Path primaryDir = Path.of(primaryPath);
        Path savedFile = Files.list(primaryDir)
            .filter(path -> path.toString().endsWith(".json"))
            .findFirst()
            .orElseThrow();

        String content = Files.readString(savedFile);

        assertThat(content).contains(topic);
        assertThat(content).contains(testEvent.getClass().getSimpleName());
        assertThat(content).contains(testError.getMessage());
        assertThat(content).contains("Kafka 발행 실패");
    }

    @Test
    @DisplayName("메모리 큐의 이벤트를 파일로 flush")
    void flushMemoryQueueToFile_flushesEventsToFile() throws IOException {
        // given
        Path fileAsPrimary = tempDir.resolve("file-as-primary3");
        Path fileAsBackup = tempDir.resolve("file-as-backup3");
        Files.writeString(fileAsPrimary, "dummy");
        Files.writeString(fileAsBackup, "dummy");

        ReflectionTestUtils.setField(handler, "primaryPath", fileAsPrimary.toString());
        ReflectionTestUtils.setField(handler, "backupPath", fileAsBackup.toString());

        handler.saveFailedEvent(topic, testEvent, testError);

        var memoryQueue = ReflectionTestUtils.getField(handler, "memoryQueue");
        assertThat(((java.util.Queue<?>) memoryQueue).size()).isEqualTo(1);

        ReflectionTestUtils.setField(handler, "primaryPath", primaryPath);

        // when
        handler.flushMemoryQueueToFile();

        // then
        assertThat(((java.util.Queue<?>) memoryQueue).size()).isEqualTo(0);

        Path primaryDir = Path.of(primaryPath);
        long fileCount = Files.list(primaryDir)
            .filter(path -> path.toString().endsWith(".json"))
            .count();

        assertThat(fileCount).isEqualTo(1);
    }

    @Test
    @DisplayName("메모리 큐가 비어있으면 flush 하지 않음")
    void flushMemoryQueueToFile_doesNothingWhenQueueEmpty() {
        // given
        var memoryQueue = ReflectionTestUtils.getField(handler, "memoryQueue");
        assertThat(((java.util.Queue<?>) memoryQueue).isEmpty()).isTrue();

        // when
        handler.flushMemoryQueueToFile();

        // then
        assertThat(((java.util.Queue<?>) memoryQueue).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("7일 이상 된 파일을 삭제")
    void cleanupOldFiles_deletesFilesOlderThan7Days() throws IOException, InterruptedException {
        // given
        Path primaryDir = Path.of(primaryPath);
        Files.createDirectories(primaryDir);

        Path oldFile = primaryDir.resolve("failed-old-event.json");
        Files.writeString(oldFile, "{}");

        long eightDaysAgo = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000);
        oldFile.toFile().setLastModified(eightDaysAgo);

        Path recentFile = primaryDir.resolve("failed-recent-event.json");
        Files.writeString(recentFile, "{}");

        assertThat(Files.exists(oldFile)).isTrue();
        assertThat(Files.exists(recentFile)).isTrue();

        // when
        handler.cleanupOldFiles();

        // then
        assertThat(Files.exists(oldFile)).isFalse();
        assertThat(Files.exists(recentFile)).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 디렉토리에서 cleanup을 실행해도 오류 미발생")
    void cleanupOldFiles_handlesNonexistentDirectory() {
        // given
        String nonexistentPath = "/nonexistent/path";
        ReflectionTestUtils.setField(handler, "primaryPath", nonexistentPath);

        // when & then
        handler.cleanupOldFiles();
    }

    @Test
    @DisplayName("여러 이벤트 순차적으로 저장")
    void saveFailedEvent_savesMultipleEventsSequentially() throws IOException {
        // given
        FeedContentUpdatedEvent event1 = new FeedContentUpdatedEvent(UUID.randomUUID(), "내용1");
        FeedContentUpdatedEvent event2 = new FeedContentUpdatedEvent(UUID.randomUUID(), "내용2");
        FeedContentUpdatedEvent event3 = new FeedContentUpdatedEvent(UUID.randomUUID(), "내용3");

        // when
        handler.saveFailedEvent(topic, event1, testError);
        handler.saveFailedEvent(topic, event2, testError);
        handler.saveFailedEvent(topic, event3, testError);

        // then
        Path primaryDir = Path.of(primaryPath);
        long fileCount = Files.list(primaryDir)
            .filter(path -> path.toString().endsWith(".json"))
            .count();

        assertThat(fileCount).isEqualTo(3);
    }

    @Test
    @DisplayName("파일명에 이벤트 타입 포함")
    void saveFailedEvent_filenameContainsEventType() throws IOException {
        // when
        handler.saveFailedEvent(topic, testEvent, testError);

        // then
        Path primaryDir = Path.of(primaryPath);
        boolean hasCorrectFilename = Files.list(primaryDir)
            .map(Path::getFileName)
            .map(Path::toString)
            .anyMatch(name -> name.contains("FeedContentUpdatedEvent") && name.endsWith(".json"));

        assertThat(hasCorrectFilename).isTrue();
    }
}