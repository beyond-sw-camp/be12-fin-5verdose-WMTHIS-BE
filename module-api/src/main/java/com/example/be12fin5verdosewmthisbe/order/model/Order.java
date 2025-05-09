package com.example.be12fin5verdosewmthisbe.order.model;

import com.example.be12fin5verdosewmthisbe.inventory.model.UsedInventory;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer tableNumber;

    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Schema(description = "주문 시간", example = "2023-10-27T10:00:00Z")
    private Timestamp createdAt;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // 무한 참조 방지
    @Schema(description = "주문에 들어온 메뉴 목록")
    @ToString.Exclude
    private List<OrderMenu> orderMenuList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    public enum OrderType {
        hall,
        coupang,
        baemin,
        HALL, DELIVERY, yogiyo
    }

    public enum OrderStatus {
        CANCELLED,
        PAID
    }
    /*
    @PrePersist
    public void prePersist() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
    */

}
        