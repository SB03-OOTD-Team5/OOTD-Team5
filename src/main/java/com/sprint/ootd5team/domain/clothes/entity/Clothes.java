package com.sprint.ootd5team.domain.clothes.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tbl_clothes")
@Entity
public class Clothes extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClothesType type;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "clothes", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ClothesAttributeValue> clothesAttributeValues = new ArrayList<>();

    public void addClothesAttributeValue(ClothesAttributeValue value) {
        clothesAttributeValues.add(value);
        value.setClothes(this);
    }

    public void updateClothesName(String name) {
        this.name = name;
    }

    public void updateClothesType(ClothesType type) {
        this.type = type;
    }

    public void updateClothesImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

//    public void updateAttributes(List<ClothesAttributeWithDefDto> newAttributes) {
//        this.clothesAttributeValues.clear();
//        if (newAttributes != null) {
//            newAttributes.forEach(attrDto -> {
//                ClothesAttribute attr = ClothesAttribute.createFromDto(attrDto, this);
//                this.clothesAttributeValues.add(attr);
//            });
//        }
//    }
}
