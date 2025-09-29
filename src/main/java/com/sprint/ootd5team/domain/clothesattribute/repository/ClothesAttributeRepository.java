package com.sprint.ootd5team.domain.clothesattribute.repository;

import com.sprint.ootd5team.domain.clothesattribute.entity.ClothesAttribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {
	boolean existsByNameIgnoreCase(String name);
}
