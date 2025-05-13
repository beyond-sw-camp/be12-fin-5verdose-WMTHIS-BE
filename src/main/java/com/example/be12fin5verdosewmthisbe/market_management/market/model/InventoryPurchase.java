package com.example.be12fin5verdosewmthisbe.market_management.market.model;

import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
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

    private String inventoryName;

    private BigDecimal quantity;

    private int price;

    private String unit;

    private purchaseStatus status;

    private purchaseMethod method;

    private Timestamp createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private StoreInventory storeInventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_sale_id")
    @Schema(description = "구매요청들이 속한 판매 테이블 정보")
    private InventorySale inventorySale;

    // 구매한 매장
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    public enum purchaseStatus {
        PENDING_APPROVAL,
        isPaymentInProgress,
        confirmDelivery,
        end,
        cancelled
    }

    public enum purchaseMethod {
        credit_card,
        kakaopay,
        cash
    }
}