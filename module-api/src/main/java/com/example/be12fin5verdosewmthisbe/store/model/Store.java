package com.example.be12fin5verdosewmthisbe.store.model;

import com.example.be12fin5verdosewmthisbe.dashboard.model.DashboardInfo;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventorySale;
import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import com.example.be12fin5verdosewmthisbe.order.model.Order;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.user.model.User;
import jakarta.persistence.*;
import lombok.*;

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

    //@Column(length=200, unique = true, nullable = false)
    private String address;

    //@Column(length=200, unique = true, nullable = false)
    private String phoneNumber;

    // 위도
    @Column
    private Double latitude;

    // 경도
    @Column
    private Double longitude;

    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    @OneToMany(mappedBy = "store")
    private List<Order> orderList = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categoryList = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menuList = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> optionList = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<StoreInventory> storeInventoryList = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<InventorySale> inventorySaleList = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<InventoryPurchase> inventoryPurchaseList = new ArrayList<>();

    @OneToMany(mappedBy = "store")
    private List<DashboardInfo> dashboardInfoList = new ArrayList<>();


}
        

