package com.example.be12fin5verdosewmthisbe.inventory.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InventoryNotUsed {
    private String name;
    private BigDecimal amount;
    private String unit;

    public static InventoryNotUsed of(String name, BigDecimal amount, String unit) {
        return InventoryNotUsed.builder()
                .name(name)
                .amount(amount)
                .unit(unit)
                .build();
    }

}
