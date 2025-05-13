package com.example.be12fin5verdosewmthisbe.market_management.market.model.dto;

import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class InventoryPurchaseDto {
    @Data
    public static class InventoryPurchaseRequestDto {
        @NotNull(message = "판매 항목 ID는 필수입니다.")
        private Long inventorySaleId;

        @NotBlank(message = "재고 이름은 필수입니다.")
        private String inventoryName;

        private Long storeInventoryId;

        @NotNull(message = "수량은 필수입니다.")
        @DecimalMin(value = "0.01", inclusive = true, message = "수량은 0보다 커야 합니다.")
        private BigDecimal quantity;

        @Min(value = 1, message = "가격은 1 이상이어야 합니다.")
        private int price;

        @NotBlank(message = "결제 방법은 필수입니다.")
        @Pattern(regexp = "credit_card|kakaopay|cash", message = "결제 방법은 credit_card, kakaopay, cash 중 하나여야 합니다.")
        private String method;
    }
    @Data
    public static class InventoryPurchaseResponseDto {
        private Long inventoryPurchaseId;
        private String buyerStoreName;
        private BigDecimal quantity;
        private int price;
        private String status;
        private String method;
        private Timestamp createdAt;

        public InventoryPurchaseResponseDto(InventoryPurchase purchase, String buyerStoreName) {
            this.inventoryPurchaseId = purchase.getId();
            this.buyerStoreName = buyerStoreName;
            this.quantity = purchase.getQuantity();
            this.price = purchase.getPrice();
            this.status = purchase.getStatus().name();
            this.method = purchase.getMethod().name();
            this.createdAt = purchase.getCreatedAt();
        }
    }
}
