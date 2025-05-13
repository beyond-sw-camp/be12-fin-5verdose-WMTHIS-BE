package com.example.be12fin5verdosewmthisbe.inventory.model;

import com.example.be12fin5verdosewmthisbe.store.model.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "top_modified_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopModifiedInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate recordDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_inventory_id")
    private StoreInventory storeInventory;

    private BigDecimal modifyQuantity;

    private BigDecimal modifyRate;
}
