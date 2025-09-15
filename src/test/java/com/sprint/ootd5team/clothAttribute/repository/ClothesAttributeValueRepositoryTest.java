package com.sprint.ootd5team.clothAttribute.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeRepository;
import com.sprint.ootd5team.domain.clothattribute.repository.ClothesAttributeValueRepository;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.clothes.repository.ClothesRepository;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import com.sprint.ootd5team.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
@DisplayName("ClothesAttributeValueRepository 테스트")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ClothesAttributeValueRepositoryTest{

	@Autowired ClothesAttributeRepository attributeRepo;
	@Autowired ClothesAttributeValueRepository cavRepo;
	@Autowired ClothesRepository clothesRepo;
	@Autowired UserRepository userRepo;

	private User owner;

	@BeforeEach
	void setUpOwner() {
		owner = new User("유저","test@test.com","password", Role.ROLE_USER);
		// User 엔티티의 NOT NULL 필드들(예: email, nickname 등)이 있다면 반드시 채워주세요.
		// ReflectionTestUtils.setField(owner, "id", ownerId); // 필요하면 수동 ID도 가능
		owner = userRepo.save(owner); // 영속화
	}

	@Test
	@DisplayName("findAllByClothesId: attribute/defs가 fetch join으로 함께 조회된다")
	void findAllByClothesId_fetchJoin() {
		// given
		Clothes clothes = saveClothes("블랙 티셔츠", ClothesType.TOP);
		ClothesAttribute material = saveAttributeWithDefs("소재", "면", "울");

		cavRepo.saveAndFlush(new ClothesAttributeValue(clothes, material, "면"));

		// when
		List<ClothesAttributeValue> list = cavRepo.findAllByClothesId(clothes.getId());

		// then
		assertThat(list).hasSize(1);
		ClothesAttributeValue loaded = list.get(0);
		assertThat(loaded.getAttribute().getName()).isEqualTo("소재");
		// fetch join으로 defs까지 로딩됨
		assertThat(loaded.getAttribute().getDefs())
			.extracting(ClothesAttributeDef::getAttDef)
			.containsExactlyInAnyOrder("면", "울");
	}

	@Test
	@DisplayName("UNIQUE(clothes_id, attribute_id): 동일 조합 중복 저장 시 제약 위반")
	void uniqueOnClothesAndAttribute() {
		// given
		Clothes clothes = saveClothes("화이트 셔츠", ClothesType.TOP);
		ClothesAttribute size = saveAttributeWithDefs("사이즈", "M");

		cavRepo.saveAndFlush(new ClothesAttributeValue(clothes, size, "M"));

		// when & then: 동일 (clothes, attribute) 조합 재삽입 → 제약 위반
		assertThatThrownBy(() ->
			cavRepo.saveAndFlush(new ClothesAttributeValue(clothes, size, "M"))
		).isInstanceOf(DataIntegrityViolationException.class);
	}

	// ===== helpers =====

	private Clothes saveClothes(String name, ClothesType type) {
		return clothesRepo.save(
			Clothes.builder()
				.owner(owner)          // PostgresTestSupport에서 제공되는 사용자
				.name(name)
				.type(type)
				.build()
		);
	}

	private ClothesAttribute saveAttributeWithDefs(String name, String... defs) {
		ClothesAttribute attr = attributeRepo.save(new ClothesAttribute(name));
		Arrays.stream(defs)
			.map(v -> new ClothesAttributeDef(attr, v))
			.forEach(attr.getDefs()::add);
		return attributeRepo.saveAndFlush(attr);
	}
}
