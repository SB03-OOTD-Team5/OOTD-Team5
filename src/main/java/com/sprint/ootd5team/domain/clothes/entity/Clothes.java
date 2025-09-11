package com.sprint.ootd5team.domain.clothes.entity;

import com.sprint.ootd5team.base.entity.BaseUpdatableEntity;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
public class Clothes extends BaseUpdatableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
//    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private ClothesType type;

    @Column(name = "image_url")
    private String imageUrl;

    public void updateClothesName(String name) {
        this.name = name;
    }

    public void updateClothesType(ClothesType type) {
        this.type = type;
    }

    public void updateClothesImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
