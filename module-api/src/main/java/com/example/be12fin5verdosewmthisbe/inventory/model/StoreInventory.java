package com.example.be12fin5verdosewmthisbe.inventory.model;

import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "store_inventory")
@Data
@NoArgsConstructor  // JPA에서 필요
@AllArgsConstructor // Builder 내부에서 사용
@Builder
@Schema(description = "매장별재고")
public class StoreInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Store_inventory_id")
    @Schema(description = "매장별재고 ID", example = "1")
    private Long id;

    @Column(name = "expiry_date")
    @Schema(description = "입고날로부터 사용가능한 유통기한", example = "5")
    private Integer expiryDate;

    @Column(name = "unit")
    @Schema(description = "사용단위", example = "kg,g,ml")
    private String unit;

    @Column(name = "quantity")
    @Schema(description = "총수량 (소수 가능)", example = "12.50(kg)")
    private BigDecimal quantity;

    @Column(name = "name")
    @Schema(description = "이름", example = "마늘")
    private String name;

    @Column(name = "minimum_quantity")
    @Schema(description = "최소수량", example = "5(kg)")
    private BigDecimal minQuantity;


    @OneToMany(mappedBy = "storeInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Schema(description = "상세 재고 목록")
    private List<Inventory> inventoryList = new ArrayList<>();


    @OneToMany(mappedBy = "storeInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "옵션 재고 사용량 목록")
    private List<OptionValue> optionValues = new ArrayList<>();

    @OneToMany(mappedBy = "storeInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Schema(description = "레시피 목록")
    private List<Recipe> recipeList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "storeInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<InventorySale> inventorySaleList = new ArrayList<>();

    @OneToMany(mappedBy = "storeInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryPurchase> inventoryPurchaseList = new ArrayList<>();

    @OneToMany(mappedBy = "storeInventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UsedInventory> usedInventorylist = new ArrayList<>();



}

