package com.sprint.ootd5team;

import com.sprint.ootd5team.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class Ootd5TeamApplicationTests {

    @Test
    void contextLoads() {
    }

}
