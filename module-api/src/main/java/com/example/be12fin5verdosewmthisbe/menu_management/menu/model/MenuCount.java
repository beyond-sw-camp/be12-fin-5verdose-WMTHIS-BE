package com.example.be12fin5verdosewmthisbe.menu_management.menu.model;

import com.example.be12fin5verdosewmthisbe.store.model.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "menu_count")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "재고 사용량")
public class MenuCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Schema(description = "메뉴 건수 id", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    @Schema(description = "사용한 메뉴")
    private Menu menu;

    @Column(name = "count")
    @Schema(description = "주문한 수량", example = "3")
    private int count;

    @Column(name = "used_date")
    @Schema(description = "사용한 날짜", example = "2025-04-01T10:00:00Z")
    private Timestamp usedDate;
}
