package com.example.be12fin5verdosewmthisbe.order.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class OrderTodayDto {
    @Getter
    @Builder
    public static class OrderTodayResponse {
        private Integer todayTotal; // 오늘 총 매출
        private Integer interval; // 7일전과 비교해서 매출
        private List<OrderTodayTime> OrderTodayTimeList;

        public static OrderTodayResponse of(
                Integer todayTotal,
                Integer interval, // 7일전과 비교해서 매출
                List<OrderTodayTime> OrderTodayTimeList
        )
        {
            return OrderTodayResponse.builder()
                    .todayTotal(todayTotal)
                    .interval(interval)
                    .OrderTodayTimeList(OrderTodayTimeList)
                    .build();
        }

    }

    @Getter
    @Builder
    public static class OrderTodayTime{

        private Integer time;
        private Integer timeHall;
        private Integer timeDelivery;

        public static OrderTodayTime of(
                Integer time,
                Integer timeHall,
                Integer timeDelivery
        )
        {
            return OrderTodayTime.builder()
                    .time(time)
                    .timeHall(timeHall)
                    .timeDelivery(timeDelivery)
                    .build();
        }


    }
}
