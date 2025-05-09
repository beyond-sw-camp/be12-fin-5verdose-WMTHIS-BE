package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class StoreInventoryDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class responseDto {
        private Long id;
        private String name;

        private Integer expiryDate;

        private BigDecimal minQuantity;

        private BigDecimal quantity;

        private String unit;
    }
}
