package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class InventoryValidateOrderDto {
    private Long tableId;
    private List<OrderMenuRequest> orderMenus;

    @Data
    public static class OrderMenuRequest {
        private Long menuId;       // 메뉴 ID
        private Integer quantity;  // 주문 수량
        private List<Long> optionIds; // 옵션 ID 리스트
    }
}