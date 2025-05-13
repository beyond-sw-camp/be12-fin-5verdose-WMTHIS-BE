package com.example.be12fin5verdosewmthisbe.menu_management.menu.model;

import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.redis.dto.RedisRecipeDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "레시피 정보")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "레시피 ID (자동 생성)", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_inventory_id")
    @Schema(description = "레시피가 속한 메뉴에 들어가는 재료 정보")
    private StoreInventory storeInventory;

    @Schema(description = "레시피 가격", example = "2500.00")
    private BigDecimal price;
    @Schema(description = "재료의 소요량", example = "30")
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    @Schema(description = "레시피가 속한 메뉴 정보")
    private Menu menu;

    public Recipe(Long recipeId, StoreInventory storeInventoryId, BigDecimal quantity) {
        this.id = recipeId;
        this.storeInventory = storeInventoryId;
        this.quantity = quantity;
    }

    public RedisRecipeDto toRedisRecipeDto() {
        return new RedisRecipeDto(
                this.id,
                this.storeInventory.getId(),  // 예시: storeInventory에서 재료 이름을 가져옴
                this.quantity
        );
    }
}