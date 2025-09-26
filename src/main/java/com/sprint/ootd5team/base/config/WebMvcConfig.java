package com.sprint.ootd5team.base.config;

import java.nio.file.Paths;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(
    name = "ootd.storage.type",
    havingValue = "local"
)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // LocalFileStorage에서 resolveUrl("/local-files/...")
        registry.addResourceHandler("/local-files/**")
            .addResourceLocations("file:" + Paths.get("uploads").toAbsolutePath() + "/");

        // Profile 쪽은 이미지 불러오기할때 URL에 /local-files/가 들어가지 않기 때문에 따로 설정했습니다.
        registry.addResourceHandler("/profiles/**")
            .addResourceLocations("file:" + Paths.get("uploads/profiles").toAbsolutePath() + "/");
    }
}


