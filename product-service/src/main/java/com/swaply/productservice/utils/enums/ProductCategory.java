package com.swaply.productservice.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductCategory {
    ELECTRONICS("ELECTRONICS"),
    WEAR("WEAR"),
    HOME_AND_GARDEN("HOME_AND_GARDEN"),
    CHILDREN("CHILDREN"),
    SPORT("SPORT"),
    CAR("CAR"),
    BOOK("BOOK"),
    BEAUTY("BEAUTY"),
    TOY("TOY"),
    FOOD("FOOD"),
    ANIMALS("ANIMALS"),
    ART("ART"),
    BUSINESS("BUSINESS"),
    SERVICES("SERVICES"),
    OTHERS("OTHERS");

    private final String displayName;

    /**
     * Enum-un adı əvəzinə (məs: "ELEKTRONIKA")
     * oxunaqlı adı (məs: "Elektronika") qaytarır.
     */
    @Override
    public String toString() {
        return this.displayName;
    }
}