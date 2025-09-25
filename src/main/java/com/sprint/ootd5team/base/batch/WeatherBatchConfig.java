package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.base.batch.dto.WeatherBatchItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WeatherBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeatherBatchDataReader weatherBatchDataReader;
    private final WeatherBatchProcessor weatherBatchProcessor;
    private final WeatherBatchWriter weatherBatchWriter;

    @Bean
    public Job weatherBatchJob() {
        return new JobBuilder("weatherJob", jobRepository)
            .start(weatherBatchStep())
            .build();
    }

    @Bean
    public Step weatherBatchStep() {
        return new StepBuilder("weatherStep", jobRepository)
            .<WeatherBatchItem, WeatherBatchItem>chunk(100, transactionManager)
            .reader(weatherBatchDataReader)
            .processor(weatherBatchProcessor)
            .writer(weatherBatchWriter)
            .build();
    }
}
