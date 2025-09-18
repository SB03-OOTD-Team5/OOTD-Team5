package com.sprint.ootd5team.base.storage;

import java.io.InputStream;

/**
 * 파일 저장소 공통 인터페이스
 * 구현체에 따라 s3, local 스토리지 사용 가능
 */
public interface FileStorage {

    String upload(String filename, InputStream input, String contentType);

    String download(String path);

    void delete(String path);

    default String resolveUrl(String path) {
        return path != null ? download(path) : null;
    }

}
