package com.example.be12fin5verdosewmthisbe.market_management.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long buyerStoreId;

    private BigDecimal quantity;

    private int price;

    private purchaseStatus status;

    private purchaseMethod method;

    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_sale_id")
    @Schema(description = "구매요청들이 속한 판매 테이블 정보")
    private InventorySale inventorySale;



    public enum purchaseStatus {
        waiting,
        payment,
        delivery,
        end,
        cancelled
    }

    public enum purchaseMethod {
        credit_card,
        kakaopay,
        cash
    }
}
