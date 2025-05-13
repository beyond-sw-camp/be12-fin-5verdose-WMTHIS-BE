package com.example.be12fin5verdosewmthisbe.redis.dto;

import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisOptionValueDto {
    private Long storeInventoryId;      // 어떤 재고인지
    private String storeInventoryName;  // 재고 이름 (가독성용)
    private BigDecimal quantity;        // 사용 수량

    public static RedisOptionValueDto fromOptionValue(OptionValue optionValue) {
        return new RedisOptionValueDto(
                optionValue.getStoreInventory().getId(),
                optionValue.getStoreInventory().getName(),
                optionValue.getQuantity()
        );
    }
}