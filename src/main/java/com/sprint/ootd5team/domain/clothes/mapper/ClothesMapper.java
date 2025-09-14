package com.sprint.ootd5team.domain.clothes.mapper;

import com.sprint.ootd5team.domain.clothes.dto.request.ClothesCreateRequest;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClothesMapper {

    Clothes toEntity(ClothesCreateRequest request);

    ClothesDto toDto(Clothes entity);
//
//    default ClothesDto toDto(Clothes clothes, @Context S3Storage s3Storage) {
//        if (clothes == null) {
//            return null;
//        }
//
//        String imageUrl = null;
//        if (clothes.getImageUrl() != null && !clothes.getImageUrl().isEmpty()) {
//            imageUrl = s3Storage.generatePresignedUrl(clothes.getImageUrl());
//        }
//        return new ClothesDto(
//            clothes.getId(),
//            clothes.getUser(),
//            clothes.getName(),
//            clothes.getType(),
//            clothes.getImageUrl()
//        );
//    }

}
