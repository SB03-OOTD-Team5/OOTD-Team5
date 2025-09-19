package com.sprint.ootd5team.clothAttribute.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeDefCreateRequest;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import com.sprint.ootd5team.testconfig.TestConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * API: /api/clothes/attribute-defs
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("ClothesAttributeController 테스트")
@Import(TestConfig.class)
@DirtiesContext(classMode = AFTER_CLASS)
class ClothesAttributeControllerTest{

	private static final Logger log = LoggerFactory.getLogger(ClothesAttributeControllerTest.class);
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper om;
	@Autowired
	UserRepository userRepo;

	private User owner;

	@BeforeEach
	void setUpOwner() {
		owner = new User("유저", "test@test.com", "password", Role.USER);
		owner = userRepo.save(owner); // 영속화(컨트롤러에서 owner 참조 시 대비)
	}


	private RequestPostProcessor auth() {
		return user(owner.getId().toString()).roles("USER"); // ROLE_USER 부여
	}

	@Test
	@DisplayName("속성 + 허용값 등록 후 목록 조회")
	void registerAndList() throws Exception {
		// given
		ClothesAttributeDefCreateRequest req1 =
			new ClothesAttributeDefCreateRequest("소재", List.of("면", "울"));
		ClothesAttributeDefCreateRequest req2 =
			new ClothesAttributeDefCreateRequest("선호연령", List.of("10대", "20대","30대"));

		// when + then: 등록
		mockMvc.perform(post("/api/clothes/attribute-defs")
				.with(auth())          // 인증 사용자 주입
				.with(csrf())          // CSRF 토큰
				.contentType(MediaType.APPLICATION_JSON)
				.content(om.writeValueAsString(req1)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("소재"))
			.andExpect(jsonPath("$.selectableValues").isArray());

		mockMvc.perform(post("/api/clothes/attribute-defs")
				.with(auth())          // 인증 사용자 주입
				.with(csrf())          // CSRF 토큰
				.contentType(MediaType.APPLICATION_JSON)
				.content(om.writeValueAsString(req2)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("선호연령"))
			.andExpect(jsonPath("$.selectableValues").isArray());

		// 목록 조회
		mockMvc.perform(get("/api/clothes/attribute-defs")
				.with(auth())         // 인증된 사용자 재사용
				.with(csrf())          // CSRF 토큰
				.param("sortBy", "createdAt")
				.param("sortDirection", "DESCENDING"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("선호연령"))
			.andExpect(jsonPath("$[0].selectableValues").isArray())
			.andExpect(jsonPath("$[1].name").value("소재"))
			.andExpect(jsonPath("$[1].selectableValues").isArray());
		mockMvc.perform(get("/api/clothes/attribute-defs")
				.with(auth())         // 인증된 사용자 재사용
				.with(csrf())          // CSRF 토큰
				.param("sortBy", "name")
				.param("sortDirection", "ASCENDING"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].name").value("선호연령"))
			.andExpect(jsonPath("$[0].selectableValues").isArray())
			.andExpect(jsonPath("$[1].name").value("소재"))
			.andExpect(jsonPath("$[1].selectableValues").isArray());
	}
}
