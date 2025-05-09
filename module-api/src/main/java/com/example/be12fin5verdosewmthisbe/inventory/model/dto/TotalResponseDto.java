package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


public class TotalResponseDto {
    @Builder
    @Getter
    public static class Response{
        private LocalDate purchaseDate;
        private LocalDate expiryDate;     // 유통기한// 단가
        private BigDecimal quantity;

        public static Response of(LocalDate purchaseDate, LocalDate expiryDate, BigDecimal quantity) {
            return Response.builder()
                    .purchaseDate(purchaseDate)  // 현재 시간으로 변환
                    .expiryDate(expiryDate)  // 이미 LocalDate 타입
                    .quantity(quantity)  // 입고 수량을 BigDecimal로 변환
                    .build();
        }
    }
    // 수량
}


