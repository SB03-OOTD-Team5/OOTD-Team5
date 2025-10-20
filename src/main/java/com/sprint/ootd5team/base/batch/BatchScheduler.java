package com.sprint.ootd5team.base.batch;

import com.sprint.ootd5team.domain.weather.service.WeatherCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final WeatherBatchConfig batchConfig;
    private final WeatherCleanupService weatherCleanupService;

    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul")
    public void runJob() {
        // job parameter 설정
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
        try {
            jobLauncher.run(batchConfig.weatherBatchJob(), jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                 | JobParametersInvalidException |
                 org.springframework.batch.core.repository.JobRestartException e) {
            log.error(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 2 ? * MON", zone = "Asia/Seoul")
    public void cleanupUnusedWeathers() {
        try {
            int deleted = weatherCleanupService.deleteUnusedWeathersBeforeToday();
            log.info("[WeatherScheduler] 삭제된 weather 컬럼 갯수: {}", deleted);
        } catch (Exception e) {
            log.error("[WeatherScheduler] weather 스케쥴링 실패. {}", e.getMessage(), e);
        }
    }
}
