package com.example.be12fin5verdosewmthisbe.inventory.model;

import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor  // JPA에서 필요
@AllArgsConstructor // Builder 내부에서 사용
@Builder
@Schema(description = "재고")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventoryid")
    @Schema(description = "재고 ID", example = "1")
    private Long inventoryId;

    @Column(name = "purchasedate")
    @Schema(description = "구매날짜", example = "2025-04-01T10:00:00Z")
    private Timestamp purchaseDate;

    @Column(name = "expirydate")
    @Schema(description = "유통기한", example = "2026-04-01")
    private LocalDate expiryDate;

    @Column(name = "unitprice")
    @Schema(description = "단가", example = "1500")
    private Integer unitPrice;

    @Column(name = "quantity")
    @Schema(description = "수량 (소수 가능)", example = "12.50(kg)")
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_inventory_id")  // 외래 키 설정
    private StoreInventory storeInventory;  // 필드 이름을 storeInventory로 설정

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Schema(description = "수정된 재고 목록")
    private List<ModifyInventory> modifyInventoryList;
}
