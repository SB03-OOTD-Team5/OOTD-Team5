package com.sprint.ootd5team.domain.clothattribute.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import jakarta.persistence.*;
import lombok.*;

/**
 * 특정 옷(tbl_clothes)에 대해
 * 어떤 속성(tbl_cloth_attributes)이
 * 어떤 값(selectable_value 문자열)으로 지정되었는지 저장하는 엔티티
 */
@Entity
@Table(name = "tbl_cloth_attributes_values",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_clothes_attributes_one_value", columnNames = {"clothes_id", "attributes_id"})
	},
	indexes = {
		@Index(name = "idx_cav_clothes_attribute", columnList = "clothes_id, attribute_id")
	})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothAttributeValue extends BaseEntity {

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
	@JoinColumn(name = "attributes_id", nullable = false)
	private ClothAttribute attribute;

	/**
	 * 선택된 값 (문자열로 저장: 면, 나일론, 여름, 겨울 등)
	 * → 순환참조 방지를 위해 selectable_values 엔티티 대신 String 사용
	 */
	@Column(name = "selectable_value", length = 50, nullable = false)
	@Setter
	private String selectableValue; // 선택된 값(문자열) → FK는 DB에서 강제

	public ClothAttributeValue(Clothes cloths, ClothAttribute attribute, String value){
		this.clothes = cloths;
		this.attribute = attribute;
		this.selectableValue = value;
	}
}
