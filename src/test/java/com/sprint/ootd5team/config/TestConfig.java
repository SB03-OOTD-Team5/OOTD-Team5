package com.sprint.ootd5team.config;

import static org.mockito.Mockito.when;

import com.sprint.ootd5team.base.cache.CacheEvictHelper;
import com.sprint.ootd5team.domain.feed.search.FeedDocument;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public CacheEvictHelper cacheEvictHelper() {
        return Mockito.mock(CacheEvictHelper.class);
    }

    @Bean
    @Primary
    public ElasticsearchOperations elasticsearchOperations() {
        ElasticsearchOperations mock = Mockito.mock(ElasticsearchOperations.class);
        IndexOperations indexOps = Mockito.mock(IndexOperations.class);

        when(mock.indexOps(FeedDocument.class)).thenReturn(indexOps);
        when(indexOps.exists()).thenReturn(true);

        return mock;
    }
}
