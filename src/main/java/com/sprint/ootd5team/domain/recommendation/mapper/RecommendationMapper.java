package com.sprint.ootd5team.domain.recommendation.mapper;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothesattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = ClothesAttributeMapper.class)
public abstract class RecommendationMapper {

    @Autowired
    protected FileStorage fileStorage;

    @Mapping(source = "id", target = "clothesId")
    @Mapping(source = "clothesAttributeValues", target = "attributes")
    @Mapping(target = "imageUrl", qualifiedByName = "resolveImageUrl")
    public abstract RecommendationClothesDto toDto(Clothes entity);

    @Named("resolveImageUrl")
    protected String resolveImageUrl(String path) {
        return fileStorage.resolveUrl(path);
    }
}