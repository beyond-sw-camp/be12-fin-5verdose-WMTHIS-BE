package com.example.be12fin5verdosewmthisbe.menu_management.option.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "옵션별 재고 사용 수량 정보")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = " ID (자동 생성)", example = "1")
    private Long id;

    @Schema(description = "재고 ID", example = "10")
    private Long inventoryId;

    @Schema(description = "사용 수량", example = "2.0")
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    @Schema(description = "해당 수량 정보가 속한 옵션 정보")
    private Option option;
}