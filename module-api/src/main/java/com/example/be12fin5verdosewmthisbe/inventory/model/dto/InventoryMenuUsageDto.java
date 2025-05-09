package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryMenuUsageDto {

    private String inventoryName;
    private BigDecimal totalUsedQuantity;
    private String unit;
    private Long storeInventoryId;

    public InventoryMenuUsageDto(String inventoryName, BigDecimal totalUsedQuantity, String unit, Long storeInventoryId) {
        this.inventoryName = inventoryName;
        this.totalUsedQuantity = totalUsedQuantity;
        this.unit = unit;
        this.storeInventoryId = storeInventoryId;
    }
}
