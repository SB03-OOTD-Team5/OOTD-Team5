package com.sprint.ootd5team.domain.extract.dto;

public record BasicClothesInfo(
    String imageUrl,
    String bodyText,
    String name
) {

    public boolean isEmpty() {
        return (imageUrl == null || imageUrl.isBlank())
            && (bodyText == null || bodyText.isBlank())
            && (name == null || name.isBlank());
    }
}