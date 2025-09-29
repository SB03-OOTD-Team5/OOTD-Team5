package com.sprint.ootd5team.base.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Slf4j
@Configuration
public class WebclientConfig {


    @Bean("kmaApiClient")
    public WebClient kmaApiClient(
        @Value("${weather.kma.base-url}") String baseUrl,
        @Value("${weather.kma.client-secret}") String secretKey
    ) {

        // 공공데이터 키 이용시 ServiceKey, 기상청KMA 직접 이용시 authKey로 헤더이름이 다름.
        //  String fixedBase = baseUrl + "?ServiceKey=" + secretKey + "&dataType=JSON";
        String fixedBase = baseUrl + "?authKey=" + secretKey + "&dataType=JSON";

        DefaultUriBuilderFactory ubf = new DefaultUriBuilderFactory(fixedBase);
        ubf.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return WebClient.builder()
            .uriBuilderFactory(ubf)
            .filter((request, next) -> {
                String url = request.url().toString()
                //    .replaceAll("(?i)(serviceKey=)[^&]+", "$1****");
                    .replaceAll("(?i)(authKey=)[^&]+", "$1****");
                log.debug("[기상청 API 리퀘스트] {} {}", request.method(), url);
                return next.exchange(request);
            })
            .build();

    }


    @Bean("kakaoApiClient")
    public WebClient kakaoApiClient(
        @Value("${weather.kakao.base-url}") String baseUrl,
        @Value("${weather.kakao.client-secret}") String secretKey
    ) {
        DefaultUriBuilderFactory ubf = new DefaultUriBuilderFactory(baseUrl);
        ubf.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);

        return WebClient.builder()
            .uriBuilderFactory(ubf)
            .defaultHeader("Authorization", "KakaoAK " + secretKey)
            .filter((request, next) -> {
                log.debug("[카카오 API 리퀘스트] {} {}", request.method(), request.url());
                return next.exchange(request);
            })
            .build();
    }

    @Bean
    public WebClient openMeteoWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.open-meteo.com/v1")
            .build();
    }

}
