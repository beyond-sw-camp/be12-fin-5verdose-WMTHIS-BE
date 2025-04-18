package com.example.be12fin5verdosewmthisbe.market_management.market.model;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.sql.Timestamp;
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

    private Long inventoryId;

    private String inventoryName;

    private Long sellerStoreId;

    private String sellerStoreName;

    private String buyerStoreName;

    private BigDecimal quantity;

    private int price;

    private saleStatus status;

    private String content;

    private Timestamp createdAt;


    @OneToMany(mappedBy = "inventorySale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "구매 요청 목록")
    private List<InventoryPurchase> purchaseList = new ArrayList<>();

    @OneToMany(mappedBy = "inventorySale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "등록된 이미지 목록")
    private List<Images> imageList = new ArrayList<>();

    public enum saleStatus {
        available,
        waiting,
        delivery,
        sold,
        cancelled
    }
}
        