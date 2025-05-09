package com.example.be12fin5verdosewmthisbe.market_management.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Images {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_sale_id")
    @Schema(description = "이미지 경로들이 속한 판매 테이블 정보")
    private InventorySale inventorySale;

    private String url;
}
