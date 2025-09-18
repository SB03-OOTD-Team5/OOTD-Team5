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
    }
}


