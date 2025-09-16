package com.sprint.ootd5team.user.controller;

import com.sprint.ootd5team.domain.user.controller.UserController;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles({"test", "securitytest"})
public class UserControllerTest {

}
