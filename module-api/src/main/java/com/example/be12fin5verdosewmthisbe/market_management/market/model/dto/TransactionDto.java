package com.example.be12fin5verdosewmthisbe.market_management.market.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {

    private Long inventoryPurchaseId;

    private Long inventorySaleId;

    // true = 판매, false = 구매
    private Boolean type;

    private String name;

    private BigDecimal quantity;

    private int price;

    private String status;

    private String otherStoreName;

    private LocalDate createdAt;

}
