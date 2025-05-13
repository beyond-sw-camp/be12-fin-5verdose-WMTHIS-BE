package com.example.be12fin5verdosewmthisbe.store.model;

import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.user.model.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=200)
    private String name;

    @Column(length=200, nullable = false)
    private String address;

    @Column(length=200, unique = true, nullable = false)
    private String phoneNumber;

    @Column(
            columnDefinition = """
        POINT
        GENERATED ALWAYS AS (
          ST_GeomFromText(
            CONCAT('POINT(', longitude, ' ', latitude, ')')
          )
        ) STORED
        """,
            nullable = true,      // Hibernate가 NOT NULL 자동 추가를 방지
            updatable = false,
            insertable = false
    )
    private byte[] location;


    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "store")
    @JsonIgnore
    private List<Order> orderList = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categoryList = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Menu> menuList = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> optionList = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<StoreInventory> storeInventoryList = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<InventorySale> inventorySaleList = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<InventoryPurchase> inventoryPurchaseList = new ArrayList<>();
}
        

