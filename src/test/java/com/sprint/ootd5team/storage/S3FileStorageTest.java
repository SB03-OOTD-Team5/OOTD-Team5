package com.sprint.ootd5team.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.sprint.ootd5team.base.storage.S3FileStorage;
import java.io.ByteArrayInputStream;
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

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3FileStorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private S3FileStorage s3FileStorage;

    @BeforeEach
    void setUp() {
        s3FileStorage = new S3FileStorage(s3Client, s3Presigner);
        ReflectionTestUtils.setField(s3FileStorage, "bucket", "test-bucket");
        ReflectionTestUtils.setField(s3FileStorage, "presignedUrlExpiration", 600);
    }

    @Test
    void 업로드_성공() {
        // given
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        // when
        String key = s3FileStorage.upload("test.jpg", inputStream);

        // then
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        assertThat(captor.getValue().bucket()).isEqualTo("test-bucket");
        assertThat(captor.getValue().key()).contains("test.jpg");
        assertThat(key).contains("test.jpg");
    }

    @Test
    void 업로드_실패시_예외발생() {
        // given
        InputStream inputStream = new ByteArrayInputStream("bad".getBytes());
        doThrow(new RuntimeException("fail")).when(s3Client)
            .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // when
        Throwable thrown = catchThrowable(() -> s3FileStorage.upload("bad.jpg", inputStream));

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("파일 업로드 실패");
    }

    @Test
    void presigned_url_생성() {
        // given
        String key = "clothes/test.jpg";
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
        String key = "clothes/test.jpg";

        // when
        s3FileStorage.delete(key);

        // then
        verify(s3Client).deleteObject(any(Consumer.class));
    }
}
