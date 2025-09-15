package com.sprint.ootd5team.domain.clothattribute.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 특정 옷(tbl_clothes)에 대해
 * 어떤 속성(tbl_cloth_attributes)이
 * 어떤 값(selectable_value 문자열)으로 지정되었는지 저장하는 엔티티
 */
@Entity
@Table(
	name = "tbl_clothes_attributes_values",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_cav_clothes_attribute",
			columnNames = {"clothes_id", "attribute_id"}
		)
	},
	indexes = {
		@Index(name = "ix_cav_cloth_attr", columnList = "clothes_id, attributes_id"),
		@Index(name = "ix_cav_attr_defvalue", columnList = "attributes_id, def_value")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothesAttributeValue extends BaseUpdatableEntity {

	/**
	 * 어떤 의상인지
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "clothes_id", nullable = false)
	private Clothes clothes;
	/**
	 * 어떤 속성인지 (예: 소재, 계절)
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attribute_id", nullable = false)
	private ClothesAttribute attribute;

	/**
	 * 선택된 값 (문자열로 저장: 면, 나일론, 여름, 겨울 등)
	 * → 순환참조 방지를 위해 selectable_values 엔티티 대신 String 사용
	 */
	@Column(name = "def_value", length = 50, nullable = false)
	@Setter
	private String selectableValue; // 선택된 값(문자열) → FK는 DB에서 강제

	public ClothesAttributeValue(Clothes cloths, ClothesAttribute attribute, String value){
		this.clothes = cloths;
		this.attribute = attribute;
		this.selectableValue = value;
	}
}
