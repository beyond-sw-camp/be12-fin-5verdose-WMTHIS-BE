package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
    public class TotalInventoryDto {

    private Integer quantity;
    // 입고 수량
    @JsonFormat(pattern = "yyyy-MM-dd")// 입고 날짜 (purchaseDate)
    private Integer expiryDateInt;  // 유통기한 (expiryDate)
    private Long storeInventoryId;  // StoreInventory ID (어떤 재고인지)

    private Integer unitPrice;

    public Inventory toEntity(StoreInventory storeInventory, TotalInventoryDto dto, LocalDate getExpiryDate) {
        return Inventory.builder()
                .purchaseDate(Timestamp.valueOf(LocalDateTime.now()))  // 현재 시간으로 변환
                .expiryDate(getExpiryDate)  // 이미 LocalDate 타입
                .quantity(new BigDecimal(dto.getQuantity()))  // 입고 수량을 BigDecimal로 변환
                .unitPrice(dto.getUnitPrice())
                .storeInventory(storeInventory)  // 해당 StoreInventory 객체를 연결
                .build();
    }

}

