package com.sprint.ootd5team.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.sprint.ootd5team.base.exception.file.FileDeleteFailedException;
import com.sprint.ootd5team.base.exception.file.FilePermanentSaveFailedException;
import com.sprint.ootd5team.base.exception.file.FileSaveFailedException;
import com.sprint.ootd5team.base.exception.file.FileTooLargeException;
import com.sprint.ootd5team.base.storage.S3FileStorage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3FileStorageTest {

    private static final String TEST_PREFIX = "prefix/";

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3FileStorage s3FileStorage;

    @BeforeEach
    void setUp() {
        s3FileStorage = new S3FileStorage(s3Client, s3Presigner, DataSize.ofMegabytes(10));
        ReflectionTestUtils.setField(s3FileStorage, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3FileStorage, "presignedUrlExpiration", 600);
    }

    @Test
    void 업로드_성공() {
        // given
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // when
        String key = s3FileStorage.upload("test.jpg", inputStream, "image/jpeg", TEST_PREFIX);

        // then
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(captor.getValue().key()).startsWith(TEST_PREFIX);
        assertThat(captor.getValue().key()).contains(".jpg");
        assertThat(key).contains(".jpg");
    }

    @Test
    void 업로드_실패시_예외발생() {
        // given
        InputStream inputStream = new ByteArrayInputStream("bad".getBytes());
        doThrow(new RuntimeException("fail")).when(s3Client)
            .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // when
        Throwable thrown = catchThrowable(
            () -> s3FileStorage.upload("bad.jpg", inputStream, "image/jpeg", "clothes")
        );

        // then
        assertThat(thrown).isInstanceOf(FilePermanentSaveFailedException.class);
    }

    @Test
    void presigned_url_생성() {
        // given
        String key = "prefix/test.jpg";
        URL fakeUrl = mock(URL.class);
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);

        given(fakeUrl.toString()).willReturn("https://fake-url.com/test.jpg");
        given(presigned.url()).willReturn(fakeUrl);
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .willReturn(presigned);

        // when
        String url = s3FileStorage.download(key);

        // then
        assertThat(url).isEqualTo("https://fake-url.com/test.jpg");
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }

    @Test
    void 삭제_성공() {
        // given
        String key = "prefix/test.jpg";

        // when
        s3FileStorage.delete(key);

        // then
        verify(s3Client).deleteObject(any(Consumer.class));
    }

    @Test
    void 업로드시_InputStream읽기실패_FileSaveFailedException() {
        // given: 항상 IOException 발생시키는 InputStream
        InputStream brokenStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("읽기 실패");
            }
        };

        // when
        Throwable thrown = catchThrowable(
            () -> s3FileStorage.upload("broken.png", brokenStream, "image/png", "clothes")
        );

        // then
        assertThat(thrown)
            .isInstanceOf(FileSaveFailedException.class);
    }

    @Test
    void 삭제_실패시_FileDeleteFailedException() {
        // given
        String key = "prefix/error.jpg";
        doThrow(new RuntimeException("delete fail"))
            .when(s3Client).deleteObject(any(Consumer.class));

        // when
        Throwable thrown = catchThrowable(() -> s3FileStorage.delete(key));

        // then
        assertThat(thrown)
            .isInstanceOf(FileDeleteFailedException.class);
    }

    @Test
    void 존재하지않는key_다운로드시_NoSuchKeyException() {
        // given
        String key = "notfound.jpg";
        given(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .willThrow(NoSuchKeyException.builder().build());

        // when
        Throwable thrown = catchThrowable(() -> s3FileStorage.download(key));

        // then
        assertThat(thrown).isInstanceOf(
            NoSuchKeyException.class
        );
    }

    @Test
    void resolveUrl_null입력시_null반환() {
        // when
        String url = s3FileStorage.resolveUrl(null);

        // then
        assertThat(url).isNull();
    }

    @Test
    void 업로드_실패_용량초과() {
        // given
        S3FileStorage smallLimitStorage = new S3FileStorage(s3Client, s3Presigner,
            DataSize.ofBytes(1));
        ReflectionTestUtils.setField(smallLimitStorage, "bucket", "test-bucket");

        InputStream inputStream = new ByteArrayInputStream("hello".getBytes());

        // when & then
        assertThatThrownBy(() ->
            smallLimitStorage.upload("big.txt", inputStream, "text/plain", "clothes")
        ).isInstanceOf(FileTooLargeException.class);
    }

    @Test
    void presignedUrl_redirect_성공() {
        // given
        String key = "prefix/test.jpg";
        String fakeUrl = "https://fake-url.com/file.png";
        S3FileStorage spyStorage = spy(s3FileStorage);
        doReturn(fakeUrl).when(spyStorage).download(key);

        // when
        var response = spyStorage.redirectToPresignedUrl(key);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(302);
        assertThat(response.getHeaders().getLocation().toString()).isEqualTo(fakeUrl);
    }

    @Test
    void 업로드시_png확장자이면_imagePng으로설정된다() {
        // given
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        // when
        s3FileStorage.upload("test.png", inputStream, "application/octet-stream", "clothes");

        // then
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        assertThat(captor.getValue().contentType()).isEqualTo("image/png");
    }

}
