package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

public class InventoryChangeDto {
    @Getter
    public static class DateRequest{
        LocalDate startDate;
        LocalDate endDate;
    }

    @Builder
    @Getter
    public static class Response {
        private Timestamp date;
        private String stockName;
        private String changeReason;
        private BigDecimal quantity;
        private String unit;


        public static Response of(Timestamp date, String stockName, String changeReason, BigDecimal quantity, String unit) {
            return Response.builder()
                    .date(date)
                    .stockName(stockName)
                    .changeReason(changeReason)
                    .quantity(quantity)
                    .unit(unit)
                    .build();
        }
    }
}
