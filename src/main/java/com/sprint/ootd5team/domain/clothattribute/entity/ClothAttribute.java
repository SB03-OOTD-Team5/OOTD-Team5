package com.sprint.ootd5team.domain.clothattribute.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 * 의상 속성 엔티티
 * 예: 소재, 사이즈, 계절
 */
@Entity
@Table(name = "tbl_cloth_attributes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ClothAttribute extends BaseEntity {

	@Column(name = "name", length = 50, nullable = false)
	private String name;            //속성명

	@OneToMany
	private List<ClothAttributeDefs> clothAttributeDefs = new ArrayList<>();

}
