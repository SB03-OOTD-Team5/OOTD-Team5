package com.sprint.ootd5team.domain.clothes.fixture;

import com.sprint.ootd5team.domain.clothattribute.dto.ClothesAttributeWithDefDto;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.user.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class ClothesFixture {

    public static Clothes createClothesEntity(User owner, String name, ClothesType type,
        String imageUrl) {
        Clothes clothes = Clothes.builder()
            .owner(owner)
            .name(name)
            .type(type)
            .imageUrl(imageUrl)
            .build();

        ReflectionTestUtils.setField(clothes, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(clothes, "createdAt", Instant.now());

        return clothes;
    }

    public static ClothesDto createClothesDto(UUID id, UUID ownerId, String name,
        ClothesType type, String imageUrl) {

        return ClothesDto.builder()
            .id(id)
            .ownerId(ownerId)
            .name(name)
            .type(type)
            .imageUrl(imageUrl)
            .attributes(List.of())
            .build();
    }

    public static List<Clothes> createTestClothes(User owner) {
        return List.of(
            createClothesEntity(owner, "흰 티셔츠", ClothesType.TOP, null),
            createClothesEntity(owner, "청바지", ClothesType.BOTTOM, null),
            createClothesEntity(owner, "운동화", ClothesType.SHOES, null)
        );
    }

    public static ClothesDto toDto(Clothes clothes) {
        return ClothesDto.builder()
            .id(clothes.getId())
            .ownerId(clothes.getOwner().getId())
            .name(clothes.getName())
            .type(clothes.getType())
            .imageUrl(clothes.getImageUrl())
            .attributes(
                clothes.getClothesAttributeValues().stream()
                    .map(v -> new ClothesAttributeWithDefDto(
                        v.getAttribute().getId(),
                        v.getAttribute().getName(),
                        v.getAttribute().getDefs().stream()
                            .map(def -> def.getAttDef())
                            .toList(),
                        v.getDefValue()
                    ))
                    .toList()
            )
            .build();
    }

    public static List<ClothesDto> createTestClothesDtos(UUID ownerId) {
        Instant now = Instant.now();
        return List.of(
            createClothesDto(UUID.randomUUID(), ownerId, "흰 티셔츠", ClothesType.TOP, null),
            createClothesDto(UUID.randomUUID(), ownerId, "청바지", ClothesType.BOTTOM, null),
            createClothesDto(UUID.randomUUID(), ownerId, "운동화", ClothesType.SHOES, null)
        );
    }

    public static ClothesAttribute createSeasonAttribute(UUID id) {
        ClothesAttribute attribute = new ClothesAttribute("계절");
        attribute.getDefs().add(new ClothesAttributeDef(attribute, "봄"));
        attribute.getDefs().add(new ClothesAttributeDef(attribute, "여름"));
        attribute.getDefs().add(new ClothesAttributeDef(attribute, "가을"));
        attribute.getDefs().add(new ClothesAttributeDef(attribute, "겨울"));
        ReflectionTestUtils.setField(attribute, "id", id);
        return attribute;
    }
}
