package com.sprint.ootd5team.domain.clothattribute.repository;

import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesAttributeRepository extends JpaRepository<ClothesAttribute, UUID> {
	Boolean existsByNameIgnoreCase(String name);
}
