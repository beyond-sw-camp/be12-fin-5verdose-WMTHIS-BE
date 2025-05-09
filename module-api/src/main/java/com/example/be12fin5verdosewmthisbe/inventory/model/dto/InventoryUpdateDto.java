package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class InventoryUpdateDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private int total;
        private List<ItemQuantityDto> itemQuantityDtoList;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemQuantityDto {
        private String itemName;
        private BigDecimal totalQuantity;
    }
}