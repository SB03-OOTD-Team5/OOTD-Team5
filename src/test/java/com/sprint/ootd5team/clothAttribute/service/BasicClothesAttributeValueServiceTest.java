package com.sprint.ootd5team.clothAttribute.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import com.sprint.ootd5team.base.security.JwtRegistry;
import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeValueRepository;
import com.sprint.ootd5team.domain.clothattribute.service.BasicClothesAttributeValueService;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("ClothesAttributeValueService 테스트")
class BasicClothesAttributeValueServiceTest{

	@TestConfiguration
	static class TestSecurityConfig {
		@Bean
		PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}
	}
	@MockitoBean
	private JwtRegistry jwtRegistry;
	@MockitoBean
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired BasicClothesAttributeValueService service;
	@Autowired ClothesRepository clothesRepo;
	@Autowired ClothesAttributeRepository attrRepo;
	@Autowired ClothesAttributeValueRepository cavRepo;
	@Autowired UserRepository userRepo;

	private User owner;

	@BeforeEach
	void setUpOwner() {
		owner = userRepo.findByEmail("test@test.com").orElse(userRepo.save(new User("유저","test@test.com","password", Role.USER)));

	}

	@Test
	@DisplayName("허용값만 저장되고, 이후 다른 허용값으로 갱신된다(upsert 시나리오)")
	void createAndUpdate() {
		// given
		Clothes clothes = clothesRepo.save(Clothes.builder()
			.owner(owner)
			.name("블랙 티셔츠")
			.type(ClothesType.TOP)
			.build());

		ClothesAttribute season = attrRepo.save(new ClothesAttribute("계절"));
		season.getDefs().add(new ClothesAttributeDef(season, "여름"));
		season.getDefs().add(new ClothesAttributeDef(season, "겨울"));
		attrRepo.saveAndFlush(season);

		// when: 최초 저장(여름)
		ClothesAttributeWithDefDto first = service.create(clothes.getId(), season.getId(), "여름");
		// then
		assertThat(first.definitionName()).isEqualTo("계절");
		assertThat(first.value()).isEqualTo("여름");

		// when: 갱신(겨울)
		ClothesAttributeWithDefDto updated = service.create(clothes.getId(), season.getId(), "겨울");
		// then
		assertThat(updated.value()).isEqualTo("겨울");

		// 저장 결과가 1건만 유지되는지 확인
		List<?> all = cavRepo.findAllByClothesId(clothes.getId());
		assertThat(all).hasSize(1);
	}

	@Test
	@DisplayName("허용되지 않은 값 저장 시 IllegalArgumentException 발생")
	void rejectNotAllowed() {
		Clothes clothes = clothesRepo.save(Clothes.builder()
			.owner(owner)
			.name("화이트 팬츠")
			.type(ClothesType.BOTTOM)
			.build());

		ClothesAttribute material = attrRepo.save(new ClothesAttribute("소재"));
		material.getDefs().add(new ClothesAttributeDef(material, "면"));
		attrRepo.saveAndFlush(material);

		assertThatThrownBy(() ->
			service.create(clothes.getId(), material.getId(), "울")) // 정의에 없음
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("허용되지 않은 속성값");
	}

	@Test
	@DisplayName("동일 옷에 서로 다른 속성 2개 저장 후, 한 속성만 변경하면 나머지는 유지된다")
	void upsertOnlyOneOfMultipleAttributes() {
		// given: 옷 + 속성(계절/길이) 정의
		Clothes clothes = clothesRepo.save(Clothes.builder()
			.owner(owner)
			.name("블랙 티셔츠")
			.type(ClothesType.TOP)
			.build());

		ClothesAttribute season = attrRepo.save(new ClothesAttribute("계절"));
		season.getDefs().add(new ClothesAttributeDef(season, "여름"));
		season.getDefs().add(new ClothesAttributeDef(season, "겨울"));
		attrRepo.saveAndFlush(season);

		ClothesAttribute length = attrRepo.save(new ClothesAttribute("길이"));
		length.getDefs().add(new ClothesAttributeDef(length, "숏"));
		length.getDefs().add(new ClothesAttributeDef(length, "롱"));
		attrRepo.saveAndFlush(length);

		// when: 최초 저장 (계절:여름, 길이:롱)
		service.create(clothes.getId(), season.getId(), "여름");
		service.create(clothes.getId(), length.getId(), "롱");

		// then: 두 건 저장 확인
		var first = cavRepo.findAllByClothesId(clothes.getId());
		assertThat(first).hasSize(2);
		assertThat(first)
			.extracting(cav -> cav.getAttribute().getName(), ClothesAttributeValue::getDefValue)
			.containsExactlyInAnyOrder(
				tuple("계절", "여름"),
				tuple("길이", "롱")
			);

		// when: 계절만 '겨울'로 갱신
		service.create(clothes.getId(), season.getId(), "겨울");

		// then: 여전히 2건이고, 계절만 변경되고 길이는 유지됨
		var second = cavRepo.findAllByClothesId(clothes.getId());
		assertThat(second).hasSize(2);

		Map<String, String> map = second.stream()
			.collect(Collectors.toMap(
				cav -> cav.getAttribute().getName(),
				ClothesAttributeValue::getDefValue
			));

		assertThat(map.get("계절")).isEqualTo("겨울");
		assertThat(map.get("길이")).isEqualTo("롱");
	}

	@Test
	@DisplayName("getByClothesId: 의상에 저장된 속성/값을 DTO로 조회한다")
	void getByClothesId_returnsDtos() {
		// given
		Clothes clothes = clothesRepo.save(Clothes.builder()
			.owner(owner)
			.name("패딩")
			.type(ClothesType.OUTER)
			.build());

		// UNIQUE(name) 충돌 방지용 접미사
		String seasonName = unique("계절");
		String lengthName = unique("길이");

		ClothesAttribute season = attrRepo.save(new ClothesAttribute(seasonName));
		season.getDefs().add(new ClothesAttributeDef(season, "봄"));
		season.getDefs().add(new ClothesAttributeDef(season, "겨울"));
		attrRepo.saveAndFlush(season);

		ClothesAttribute length = attrRepo.save(new ClothesAttribute(lengthName));
		length.getDefs().add(new ClothesAttributeDef(length, "숏"));
		length.getDefs().add(new ClothesAttributeDef(length, "롱"));
		attrRepo.saveAndFlush(length);

		cavRepo.saveAndFlush(new ClothesAttributeValue(clothes, season, "겨울"));
		cavRepo.saveAndFlush(new ClothesAttributeValue(clothes, length, "롱"));

		// when
		List<ClothesAttributeWithDefDto> dtos = service.getByClothesId(clothes.getId());
		System.out.println("dtos = " + dtos);
		// then
		assertThat(dtos).hasSize(2);
		Map<String, String> nameToValue = dtos.stream()
			.collect(Collectors.toMap(ClothesAttributeWithDefDto::definitionName,
				ClothesAttributeWithDefDto::value));
		assertThat(nameToValue)
			.containsEntry(seasonName, "겨울")
			.containsEntry(lengthName, "롱");
	}

	/* 테스트 내에서 간단히 유니크 이름 생성 */
	private static String unique(String base) {
		return base + "_" + System.nanoTime();
	}
}
