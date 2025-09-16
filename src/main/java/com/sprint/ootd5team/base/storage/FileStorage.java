package com.sprint.ootd5team.base.storage;

import java.io.InputStream;

public interface FileStorage {

    String upload(String filename, InputStream input);

    String download(String path);

    void delete(String path);

}
