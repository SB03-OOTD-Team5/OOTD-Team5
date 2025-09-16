package com.sprint.ootd5team.testconfig;


import com.sprint.ootd5team.base.security.JwtRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {

	@Bean
	@Primary
	public JwtRegistry jwtRegistry() {
		return Mockito.mock(JwtRegistry.class);
	}

	@Bean
	@Primary
	public RedisTemplate<String, Object> redisTemplate() {
		return Mockito.mock(RedisTemplate.class);
	}

	@Bean
	@Primary
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
