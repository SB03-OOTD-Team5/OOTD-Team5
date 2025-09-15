package com.sprint.ootd5team.clothes.fixture;

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
        ClothesType type, String imageUrl, Instant createdAt, Instant updatedAt) {

        return ClothesDto.builder()
            .id(id)
            .ownerId(ownerId)
            .name(name)
            .type(type)
            .imageUrl(imageUrl)
            .attributes(List.of())
            .createdAt(createdAt)
            .updatedAt(updatedAt)
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
            .attributes(List.of())
            .createdAt(clothes.getCreatedAt())
            .updatedAt(clothes.getUpdatedAt())
            .build();
    }

    public static List<ClothesDto> createTestClothesDtos(UUID ownerId) {
        Instant now = Instant.now();
        return List.of(
            createClothesDto(UUID.randomUUID(), ownerId, "흰 티셔츠", ClothesType.TOP, null,
                now.minusSeconds(3600), now.minusSeconds(1800)),
            createClothesDto(UUID.randomUUID(), ownerId, "청바지", ClothesType.BOTTOM, null,
                now.minusSeconds(7200), now.minusSeconds(3600)),
            createClothesDto(UUID.randomUUID(), ownerId, "운동화", ClothesType.SHOES, null,
                now.minusSeconds(10800), now.minusSeconds(5400))
        );
    }
}
