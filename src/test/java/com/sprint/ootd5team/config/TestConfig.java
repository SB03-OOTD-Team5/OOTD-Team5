package com.sprint.ootd5team.config;

import static org.mockito.Mockito.mock;

import com.sprint.ootd5team.base.cache.CacheEvictHelper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public CacheEvictHelper cacheEvictHelper() {
        return mock(CacheEvictHelper.class);
    }

    @Bean
    public S3Client s3Client() {
        return mock(S3Client.class);
    }

    @Bean
    public S3Presigner s3Presigner() {
        return mock(S3Presigner.class);
    }
}
