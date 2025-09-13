package com.sprint.ootd5team.base.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "CustomHeaderAuth";

    @Value("${swagger.server-url}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("옷장을 부탁해 API 문서")
                .description("옷장을 부탁해 프로젝트의 Swagger API 문서입니다.")
                .version("v1.0")
            )
            .servers(List.of(
                new Server()
                    .url(swaggerServerUrl)
                    .description("Generated server url")
            ))
//            .components(new Components()
//                .addSecuritySchemes(SECURITY_SCHEME_NAME,
//                    new SecurityScheme()
//                        .name("Deokhugam-Request-User-ID")
//                        .type(SecurityScheme.Type.APIKEY)
//                        .in(SecurityScheme.In.HEADER)
//                )
//            )
            .tags(List.of(
                new Tag().name("의상 속성 정의").description("의상 속성 정의 관련 API"),
                new Tag().name("피드 관리").description("피드 관련 API"),
                new Tag().name("인증 관리").description("인증 관련 API"),
                new Tag().name("알림 관리").description("알림 관련 API"),
                new Tag().name("팔로우 관리").description("팔로우 관련 API"),
                new Tag().name("의상 관리").description("의상 관련 API"),
                new Tag().name("추천 관리").description("추천 관련 API"),
                new Tag().name("날씨 관리").description("날씨 관련 API"),
                new Tag().name("프로필 관리").description("프로필 관련 API")
            ));

    }
}
