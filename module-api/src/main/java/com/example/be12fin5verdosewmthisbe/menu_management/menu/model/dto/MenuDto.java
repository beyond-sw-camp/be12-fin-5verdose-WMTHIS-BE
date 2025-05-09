package com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class MenuDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MenuListResponseDto {
        private Long id;
        private String name;
        private String category;
        private String ingredients;

    }
    @Getter
    @Builder
    public static class MenuDetailResponseDto {
        private Long id;
        private String name;
        private Long categoryId;
        private int price;
        private List<IngredientInfoDto> ingredients;
    }

    @Getter
    @Builder
    public static class IngredientInfoDto {
        private Long storeInventoryId;
        private String name;
        private BigDecimal quantity;
        private String unit;
    }
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class POSMenuListResponseDto {
        private Long id;
        private String name;
        private Long category;
        private int price;
    }
}
