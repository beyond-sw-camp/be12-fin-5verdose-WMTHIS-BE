package com.example.be12fin5verdosewmthisbe.order.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

public class OrderMonthDto {

    @Builder
    @Getter
    public static class TotalRequest{
        @NotNull(message = "월은 필수 입니다")
        private int month;
        @NotNull(message = "년도는 필수입니다")
        private int year;
    }


    @Builder
    @Getter
    public static class TotalSaleResponse{
        private Date date;
        private Integer sales;
        private Integer number; // 건수

        public static TotalSaleResponse of(Date date, Integer sales, Integer number) {
            return TotalSaleResponse.builder()
                    .date(date)
                    .sales(sales)
                    .number(number)
                    .build();
        }
    }

}
