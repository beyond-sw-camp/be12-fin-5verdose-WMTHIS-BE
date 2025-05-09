package com.example.be12fin5verdosewmthisbe.order.model.dto;

import lombok.Builder;
import lombok.Getter;

public class OrderTopMenuDto {
    @Getter
    @Builder
    public static class TopWeekResponse{
        private String Top1;
        private String Top2;
        private String Top3;
        public static TopWeekResponse of(String Top1, String Top2, String Top3){
            return TopWeekResponse.builder()
                    .Top1(Top1)
                    .Top2(Top2)
                    .Top3(Top3)
                    .build();
        }
    }
}
