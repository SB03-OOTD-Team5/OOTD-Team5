package com.sprint.ootd5team.base.config;


import java.util.List;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@EnableAsync
@Configuration
public class AsyncConfig {


    /**
     * 이벤트 처리용 전용 스레드풀 Bean 등록
     *    - CorePoolSize: 항상 유지되는 최소 스레드 수
     *    - MaxPoolSize: 최대 스레드 수
     *    - QueueCapacity: 대기할 수 있는 작업 큐 크기
     *    - ThreadNamePrefix: 생성되는 스레드 이름 접두사
     *    - TaskDecorator: 스레드 풀에서 실행될 Runnable에 컨텍스트를 전달하는 장치 (MDC, SecurityContext 등)
     * @return 생성된 TaskExecutor Bean
     */
    @Bean(name = "eventTaskExecutor")
    public TaskExecutor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);      // 기본적으로 유지할 스레드 수
        executor.setMaxPoolSize(4);       // 동시에 실행 가능한 최대 스레드 수
        executor.setQueueCapacity(100);   // 처리 대기 큐 크기
        executor.setThreadNamePrefix("event-task-"); // 생성되는 스레드 이름 접두사
        executor.setTaskDecorator(
            // 여러 TaskDecorator를 묶어서 적용 (MDC, SecurityContext)
            new CompositeTaskDecorator(List.of(mdcTaskDecorator(), securityContextTaskDecorator())));
        executor.initialize();
        return executor;
    }


    /**
     * 로깅 정보를 실행 스레드에 전달하기 위한 TaskDecorator
     *  - 다른 스레드에서 실행되더라도 요청 단위 식별자(RequestId)가 유지되도록 해줌
     * @return TaskDecorator Bean
     */
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            // 현재 스레드의 MDC에서 requestId를 꺼내둠
            Optional<String> requestId = Optional.ofNullable(MDC.get(MDCLoggingInterceptor.REQUEST_ID))
                .map(String.class::cast);
            return () -> {
                // 새로운 스레드에서 실행될 때 MDC에 requestId를 세팅
                requestId.ifPresent(id -> MDC.put(MDCLoggingInterceptor.REQUEST_ID, id));
                try {
                    runnable.run(); // 실제 로직 실행
                } finally {
                    // 실행이 끝나면 MDC에서 requestId 제거 (메모리 누수 방지)
                    requestId.ifPresent(id -> MDC.remove(MDCLoggingInterceptor.REQUEST_ID));
                }
            };
        };
    }


    /**
     * Spring Security의 SecurityContext를 전달하기 위한 TaskDecorator
     *   - 다른 스레드에서도 인증/인가 정보(SecurityContext)가 유지되도록 해줌
     * @return TaskDecorator Bean
     */
    public TaskDecorator securityContextTaskDecorator() {
        return runnable -> {
            // 현재 스레드(SecurityContextHolder)에 있는 인증 정보를 복사
            SecurityContext securityContext = SecurityContextHolder.getContext();
            return () -> {
                // 새로운 스레드에서 실행 시, SecurityContext를 세팅
                SecurityContextHolder.setContext(securityContext);
                try {
                    runnable.run(); // 실제 로직 실행
                } finally {
                    // 실행 후에는 SecurityContext 제거 (다른 요청에 섞이지 않도록)
                    SecurityContextHolder.clearContext();
                }
            };
        };
    }

}
