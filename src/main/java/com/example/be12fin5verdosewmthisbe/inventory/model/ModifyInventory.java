package com.example.be12fin5verdosewmthisbe.inventory.model;

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
@Table(name = "modifyinventory")
@Data
@NoArgsConstructor  // JPA에서 필요
@AllArgsConstructor // Builder 내부에서 사용
@Builder
@Schema(description = "수정한 재고")
public class ModifyInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "수정한재고 ID", example = "1")
    private Long id;

    @Column(name = "modify_date")
    @Schema(description = "수정한 날짜", example = "2025-04-01T10:00:00Z")
    private Timestamp modifyDate;

    @Column(name = "modify_quantity")
    @Schema(description = "수정한 수량", example = "3")
    private BigDecimal modifyQuantity;

    @Column(name = "modify_rate")
    @Schema(description = "변경 비율 (변경량 / 기존 수량 * 100)", example = "25.0")
    private BigDecimal modifyRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_inventory_id")// 외래 키 설정
    private StoreInventory storeInventory;

}
