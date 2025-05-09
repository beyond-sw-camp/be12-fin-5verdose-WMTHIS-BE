package com.example.batch.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class OrderSalesSummaryDto {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderSalesSummaryItem {
        private LocalDate saleDate;    // 날짜 (ex. 2025-05-08)
        private String saleMethod;     // HALL / DELIVERY
        private int saleCount;         // 판매 건수
        private int totalPrice;        // 총 판매 금액
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderSalesSummaryResult {
        private String month;                             // yyyy-MM 형식
        private java.util.List<OrderSalesSummaryItem> salesDetails; // 일별/방식별 매출 목록
    }
}
