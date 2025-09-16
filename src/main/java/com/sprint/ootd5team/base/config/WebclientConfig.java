package com.sprint.ootd5team.base.config;


import java.net.MalformedURLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Slf4j
@Configuration
public class WebclientConfig {


    @Bean
    public WebClient kmaApiClient(
        @Value("${weather.kma.base-url}") String baseUrl,
        @Value("${weather.kma.client-secret}") String secretKey
    ) throws MalformedURLException {

        String fixedBase = baseUrl + "?ServiceKey=" + secretKey + "&dataType=JSON";

        DefaultUriBuilderFactory ubf = new DefaultUriBuilderFactory(fixedBase);
        ubf.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return WebClient.builder()
            .uriBuilderFactory(ubf)
            .filter((request, next) -> {
                log.debug("[WebClient 리퀘스트] {} {}", request.method(), request.url());
                return next.exchange(request);
            })
            .build();

    }
}
