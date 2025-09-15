package com.sprint.ootd5team;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Ootd5TeamApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ootd5TeamApplication.class, args);
    }

}
