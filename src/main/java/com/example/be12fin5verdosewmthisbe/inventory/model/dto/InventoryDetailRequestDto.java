package com.example.be12fin5verdosewmthisbe.inventory.model.dto;


import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Builder
public class InventoryDetailRequestDto {

    private Integer inventoryId;


    @NotBlank(message = "재고명은 필수입니다.")
    @Schema(description = "재고명", required = true, example = "name")
    private String name;

    @NotNull(message = "유통기한은 필수입니다.")
    @Schema(description = "유통기한", required = true, example = "2")
    private Integer expiryDate;

    @Min(value = 1, message = "최소수량은 1 이상이어야 합니다.")
    @Schema(description = "최소수량", required = true, example = "2")
    private Integer miniquantity;

    @NotBlank(message = "단위는 필수입니다.")
    @Schema(description = "용량/단위", required = true, example = "12kg")
    private String unit;

    public StoreInventory toEntity() {
        return StoreInventory.builder()
                .name(this.name)
                .expiryDate(this.expiryDate)
                .miniquantity(this.miniquantity)
                .unit(this.unit)
                .build();
    }
}
