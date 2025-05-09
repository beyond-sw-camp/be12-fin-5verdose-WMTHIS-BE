package com.example.be12fin5verdosewmthisbe.order.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class OrderSaleDetailDto {

    @Getter
    public static class OrderSaleDetailRequest{
        LocalDate startDate;
        LocalDate endDate;
    }

    @Getter
    @Builder
    public static class TotalResponse{
        String monthDateTime;
        List<OneTimeResponse> oneTimeResponseList;
        public static TotalResponse of(String monthDateTime, List<OneTimeResponse> oneTimeResponseList){
            return TotalResponse.builder()
                    .monthDateTime(monthDateTime)
                    .oneTimeResponseList(oneTimeResponseList)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OneTimeResponse{
        String saleMethod;
        int saleQuantity;
        int salePrice;

        public static OneTimeResponse of(String saleMethod, int saleQuantity, int salePrice){
            return OneTimeResponse.builder()
                    .saleMethod(saleMethod)
                    .saleQuantity(saleQuantity)
                    .salePrice(salePrice)
                    .build();
        }

    }
}
