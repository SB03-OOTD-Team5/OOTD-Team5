package com.sprint.ootd5team.domain.clothattribute.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 의상 속성 엔티티
 * 예: 소재, 사이즈, 계절
 */
@Entity
@Table(
	name = "tbl_clothes_attributes",
	uniqueConstraints = {
		@UniqueConstraint(name = "ux_attr_name", columnNames = {"name"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesAttribute extends BaseUpdatableEntity {

	@Column(name = "name", length = 50, nullable = false)
	private String name;            //속성명

	@Setter
	@OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ClothesAttributeDef> defs = new ArrayList<>(); // 카테고리의 허용값 목록

	public ClothesAttribute(String name) {
		this.name = name;
	}
}
