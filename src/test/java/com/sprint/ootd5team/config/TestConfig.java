package com.sprint.ootd5team.config;

import com.sprint.ootd5team.base.cache.CacheEvictHelper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public CacheEvictHelper cacheEvictHelper() {
        return Mockito.mock(CacheEvictHelper.class);
    }
}
