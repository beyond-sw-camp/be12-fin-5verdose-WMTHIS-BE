package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public class InventoryCallDto {
    @Builder
    @Getter
    public static class Response {
        private int expiringCount;
        private int reorderRequiredCount;
        private int receivedTodayCount;


        public static Response of(int expiringCount, int reorderRequiredCount, int receivedTodayCount) {
            return Response.builder()
                    .expiringCount(expiringCount)
                    .reorderRequiredCount(reorderRequiredCount)
                    .receivedTodayCount(receivedTodayCount)
                    .build();
        }
    }
}
