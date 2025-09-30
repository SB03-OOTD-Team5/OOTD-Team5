package com.sprint.ootd5team.domain.clothesattribute.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

import com.sprint.ootd5team.base.config.JpaAuditingConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttributeDef;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
@DisplayName("ClothesAttributeRepository 테스트")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = AFTER_CLASS)
@TestClassOrder(ClassOrderer.DisplayName.class)
class ClothesAttributeRepositoryTest{

	@Autowired
	ClothesAttributeRepository attributeRepo;
	@Autowired
	EntityManager em;

	@Test
	@DisplayName("2. 고유값 검증 : 같은 속성 내 att_def 중복 저장 시 UNIQUE(attribute_id, att_def) 위반")
	void uniqueOnAttributeAndAttdef() {
		// given
		ClothesAttribute attr = new ClothesAttribute("소재");
		attr.getDefs().add(new ClothesAttributeDef(attr, "면"));
		attr.getDefs().add(new ClothesAttributeDef(attr, "면")); // 중복

		// when + then
		assertThatThrownBy(() -> attributeRepo.saveAndFlush(attr))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	@DisplayName("1. 저장/조회: 정의 목록 저장 및 조회")
	void saveAndLoadDefs() {
		// given
		ClothesAttribute attr = new ClothesAttribute("계절");
		attr.getDefs().addAll(List.of(
			new ClothesAttributeDef(attr, "봄"),
			new ClothesAttributeDef(attr, "여름"),
			new ClothesAttributeDef(attr, "겨울")
		));

		// when: 저장
		ClothesAttribute saved = attributeRepo.saveAndFlush(attr);

		// then: 저장된 영속 객체 1차 검증
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getDefs()).hasSize(3);

		// when: 1차 캐시 제거 후 DB에서 재조회
		em.clear();
		ClothesAttribute loaded = attributeRepo.findById(saved.getId())
			.orElseThrow();

		// then: DB 재조회 결과 검증
		assertThat(loaded.getName()).isEqualTo("계절");
		assertThat(loaded.getDefs()).hasSize(3);
		assertThat(loaded.getDefs())
			.extracting(ClothesAttributeDef::getAttDef)
			.containsExactlyInAnyOrder("봄", "여름", "겨울");
	}
}
