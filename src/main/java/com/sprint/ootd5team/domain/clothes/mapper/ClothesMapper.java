package com.sprint.ootd5team.domain.clothes.mapper;

import com.sprint.ootd5team.base.storage.FileStorage;
import com.sprint.ootd5team.domain.clothesattribute.mapper.ClothesAttributeMapper;
import com.sprint.ootd5team.domain.clothes.dto.response.ClothesDto;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Clothes 엔티티를 ClothesDto로 변환하는 매퍼 FileStorage를 이용하여 이미지 Url을 접근 가능 url로 변환
 */
@Mapper(componentModel = "spring", uses = ClothesAttributeMapper.class)
public abstract class ClothesMapper {

    @Autowired
    protected FileStorage fileStorage;

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "clothesAttributeValues", target = "attributes")
    @Mapping(target = "imageUrl", qualifiedByName = "resolveImageUrl")
    public abstract ClothesDto toDto(Clothes entity);

    @Named("resolveImageUrl")
    protected String resolveImageUrl(String path) {
        return fileStorage.resolveUrl(path);
    }
}