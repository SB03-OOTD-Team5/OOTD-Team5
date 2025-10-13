package com.sprint.ootd5team.domain.recommendation.mapper;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothesattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.recommendation.dto.ClothesFilteredDto;
import com.sprint.ootd5team.domain.recommendation.dto.RecommendationClothesDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = ClothesAttributeMapper.class)
public abstract class RecommendationMapper {

    @Autowired
    protected FileStorage fileStorage;

    /** Clothes → RecommendationClothesDto (응답용) */
    @Mapping(source = "imageKey", target = "imageUrl", qualifiedByName = "resolveImageUrl")
    public abstract RecommendationClothesDto toDto(ClothesFilteredDto entity);

    /** Clothes → ClothesFilteredDto (필터링용) */
    @Mapping(source = "id", target = "clothesId")
    @Mapping(source = "imageUrl", target = "imageKey")
    @Mapping(source = "clothesAttributeValues", target = "attributes")
    public abstract ClothesFilteredDto toFilteredDto(Clothes entity);

    @Named("resolveImageUrl")
    protected String resolveImageUrl(String path) {
        return fileStorage.resolveUrl(path);
    }
}