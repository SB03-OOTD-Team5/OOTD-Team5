package com.sprint.ootd5team;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@SpringBootApplication
public class Ootd5TeamApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ootd5TeamApplication.class, args);
    }

}
