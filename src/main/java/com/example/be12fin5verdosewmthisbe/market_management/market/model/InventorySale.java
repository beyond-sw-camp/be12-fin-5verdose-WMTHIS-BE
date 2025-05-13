package com.example.be12fin5verdosewmthisbe.market_management.market.model;


import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySale {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_inventory_id")
    private StoreInventory storeInventory;

    private String inventoryName;

    private Long inventoryPurchaseId;

    private String sellerStoreName;

    private String buyerStoreName;

    private BigDecimal quantity;

    private int price;

    private String unit;

    private saleStatus status;

    private String content;

    private LocalDate expiryDate;

    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;


    @OneToMany(mappedBy = "inventorySale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "구매 요청 목록")
    private List<InventoryPurchase> purchaseList = new ArrayList<>();

    @OneToMany(mappedBy = "inventorySale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "등록된 이미지 목록")
    private List<Images> imageList = new ArrayList<>();

    public enum saleStatus {
        available,
        waiting,
        isPaymentPending,
        delivery,
        sold,
        cancelled
    }
}
        