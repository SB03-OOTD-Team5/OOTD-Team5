package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.domain.location.dto.data.LocationWithProfileIds;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WeatherBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemStreamReader<LocationWithProfileIds> weatherBatchDataReader;
    private final WeatherBatchWriter weatherBatchWriter;

    @Bean
    public Job weatherBatchJob() {
        return new JobBuilder("weatherJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(weatherBatchStep())
            .listener(weatherJobLoggingListener())
            .build();
    }

    @Bean
    public Step weatherBatchStep() {
        return new StepBuilder("weatherStep", jobRepository)
            .<LocationWithProfileIds, LocationWithProfileIds>chunk(100, transactionManager)
            .reader(weatherBatchDataReader)
            .writer(weatherBatchWriter)
            .build();
    }

    @Bean
    public JobExecutionListener weatherJobLoggingListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("[WeatherBatchJob] Job 시작 - executionId={}, jobName={}",
                    jobExecution.getId(), jobExecution.getJobInstance().getJobName());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                log.info("[WeatherBatchJob] Job 종료 - executionId={}, status={}",
                    jobExecution.getId(), jobExecution.getStatus());
            }
        };
    }
}
