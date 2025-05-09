package com.example.be12fin5verdosewmthisbe.inventory.model.dto;


import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDto {

    // 상세 재고의 id
    private Long id;
    // 상세 재고 입고날짜
    private Timestamp purchaseDate;
    // 상세 재고 유통기한
    private LocalDate expiryDate;
    // 상세 재고의 수량
    private BigDecimal quantity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryRegisterDto {
        // 입고할 재고
        @NotNull(message = "storeInventoryId는 필수입니다.")
        private Long storeInventoryId;
        // 입고할 재고의 수량
        @NotNull(message = "수량은 필수입니다.")
        @DecimalMin(value = "0.1", inclusive = true, message = "수량은 0.1 이상이어야 합니다.")
        @Schema(description = "수량", required = true, example = "2")
        private BigDecimal quantity;
        // 입고할 재고의 가격
        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        private Integer price;

    }
    
    @Data
    public static class InventoryUpdateDto {
        // 수정할 재고의 아이디
        @NotNull(message = "inventoryId는 필수입니다.")
        private Long inventoryId;
        // 수정할 재고의 유통기한
        @NotNull(message = "유통기한은 필수입니다.")
        private LocalDate expiryDate;
        // 수정할 재고의 수량
        @NotNull(message = "수량은 필수입니다.")
        @DecimalMin(value = "0.1", inclusive = true, message = "수량은 0.1 이상이어야 합니다.")
        private BigDecimal quantity;
        
    }


}
        