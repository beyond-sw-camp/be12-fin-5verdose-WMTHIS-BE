package com.example.orderservice.order.model.dto;


import com.example.orderservice.order.model.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDto {
    @Data
    public static class OrderCreateRequest {
        private Long storeId;
        private Integer tableNumber;
        private String orderType;
        private List<OrderMenuRequest> orderMenus;
    }

    @Data
    public static class OrderMenuRequest {
        private Long menuId;
        private Integer quantity;
        private Integer price;
        private List<Long> optionIds;
    }

    @Data
    @Builder
    public static class OrderCreateResponse {
        private Long id;
        private Integer tableNumber;
        private Integer totalPrice;
        private Order.OrderStatus status;
        private Timestamp createdAt;
        private Order.OrderType orderType;
        private List<OrderMenuResponse> orderMenus;


        public static OrderCreateResponse toOrderCreateResponse(Order order) {
            List<OrderMenuResponse> orderMenuResponses = order.getOrderMenuList().stream()
                    .map(orderMenu -> OrderMenuResponse.builder()
                            .menuId(orderMenu.getId())
                            .quantity(orderMenu.getQuantity())
                            .price(orderMenu.getPrice())
                            .optionIds(orderMenu.getOrderOptionList().stream()
                                    .map(orderOption -> orderOption.getOption().getId())
                                    .collect(Collectors.toList()))
                            .build())
                    .collect(Collectors.toList());

            return OrderCreateResponse.builder()
                    .id(order.getId())
                    .tableNumber(order.getTableNumber())
                    .totalPrice(order.getTotalPrice())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .orderType(order.getOrderType())
                    .orderMenus(orderMenuResponses)
                    .build();
        }
    }

    @Data
    @Builder
    public static class OrderMenuResponse {
        private Long menuId;
        private Integer quantity;
        private Integer price;
        private List<Long> optionIds;
    }

    @Builder
    @Data
    public static class AllOrderList {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private Timestamp createdAt;
        private Order.OrderType orderType;
        private int totalPrice;

        public static AllOrderList toAllOrderList(Order order) {
             return AllOrderList.builder()
                    .createdAt(order.getCreatedAt())
                    .orderType(order.getOrderType())
                    .totalPrice(order.getTotalPrice())
                    .build();
        }
    }
}