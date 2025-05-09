package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryInfoDto {
    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        // 재고 표준정보 id
        private Long id;

        // 재고 표준정보 이름
        private String name;

        // 재고의 총 수량
        private BigDecimal quantity;

        // 재고의 단위
        private String unit;

        // 재고의 유통기한
        private LocalDate expiryDate;

        // 재고의 최소 보유 수량
        private BigDecimal minQuantity;


    }
}
