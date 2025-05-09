package com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.time.LocalDate;

public class MenuSaleDto {
    @Getter
    public static class DateRequest {

        @NotNull(message = "시작 날짜는 필수입니다.")
        private LocalDate startDate;

        @NotNull(message = "종료 날짜는 필수입니다.")
        private LocalDate endDate;
    }

    @Builder
    @Getter
    public static class Response{
        private Timestamp date;
        private String category;
        private String menuName;
        private int quantity;

        public static Response of(Timestamp date, String category, String menuName, int quantity) {
            return Response.builder()
                    .date(date)
                    .menuName(menuName)
                    .category(category)
                    .quantity(quantity)
                    .build();
        }
    }
}
