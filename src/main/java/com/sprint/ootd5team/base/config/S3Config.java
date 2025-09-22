package com.sprint.ootd5team.base.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(
    name = "ootd.storage.type",
    havingValue = "s3"
)
public class S3Config {

    @Value("${ootd.storage.s3.access-key}")
    private String accessKey;

    @Value("${ootd.storage.s3.secret-key}")
    private String secretKey;

    @Value("${ootd.storage.s3.region}")
    private String region;

    @Bean
    public AwsBasicCredentials awsBasicCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public S3Client s3Client(AwsBasicCredentials awsBasicCredentials) {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsBasicCredentials awsBasicCredentials) {
        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }
}
