package com.example.be12fin5verdosewmthisbe.inventory.model;

import com.example.be12fin5verdosewmthisbe.order.model.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "usedInventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재고 사용량")
public class UsedInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "재고 사용량 id", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeinventory_id")// 외래 키 설정
    private StoreInventory storeInventory;

    @Column(name = "totalquantity")
    @Schema(description = "총수량 (소수 가능)", example = "12.50(kg)")
    private BigDecimal totalquantity;

    @Column(name = "used_date")
    @Schema(description = "사용한 날짜", example = "2025-04-01T10:00:00Z")
    private Timestamp usedDate;

    @Schema(description = "재고 이름", example = "마늘")
    private String name;

    @Schema(description = "수정한 이유", example = "true")
    private Boolean status;
    // true면 메뉴로 사용
    // false면 장터 이용으로 사용

}
