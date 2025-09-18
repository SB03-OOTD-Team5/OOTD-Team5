package com.sprint.ootd5team.domain.clothattribute.entity;

import com.sprint.ootd5team.base.entity.BaseEntity;
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
public class ClothesAttribute extends BaseEntity {

	@Column(name = "name", length = 50, nullable = false)
	private String name;            //속성명

	@OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ClothesAttributeDef> defs = new ArrayList<>(); // 카테고리의 허용값 목록

	public ClothesAttribute(String name) {
		this.name = name;
	}

	// 이름만 변경
	public void rename(String newName) {
		this.name = newName;
	}
//
//	// 기존 정의 전부 제거 후 새 정의 추가
//	public void replaceDefs(List<ClothesAttributeDef> newDefs) {
//		for (var it = defs.iterator(); it.hasNext(); ) {
//			ClothesAttributeDef def = it.next();
//			it.remove();           // ✔ orphanRemoval 트리거
//			def.setAttribute(null);  // 양방향 정합성 유지(없어도 orphanRemoval이면 삭제됨)
//		}
//
//		// 2) 새 자식들 추가 (양방향 세팅)
//		for (ClothesAttributeDef newDef : newDefs) {
//			addDef(newDef);
//		}
//	}

	// def 추가
	public void addDef(ClothesAttributeDef newDef) {
		defs.add(newDef);
		newDef.setAttribute(this);
	}
//
//	// def 제거
//	public void removeDef(ClothesAttributeDef def) {
//		defs.remove(def);
//		def.setAttribute(null);
//	}

	// def전부 비우기
	public void clearDefs() {
		for (var it = defs.iterator(); it.hasNext(); ) {
			ClothesAttributeDef def = it.next();
			it.remove();
			def.setAttribute(null);
		}
	}
}
