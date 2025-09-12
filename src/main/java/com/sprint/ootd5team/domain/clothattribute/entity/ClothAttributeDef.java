package com.sprint.ootd5team.domain.clothattribute.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_cloth_attributes_defs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothAttributeDef extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "attribute_id", nullable = false)
	private ClothAttribute attribute; // 어떤 상위 속성 카테고리에 속하는지

	@Column(name = "values", length = 50,nullable = false)
	private String value; // 실제 하위 속성값 (예: 면, 나일론)

	public ClothAttributeDef(ClothAttribute attribute, String value) {
		this.attribute = attribute;
		this.value = value;
	}
}
