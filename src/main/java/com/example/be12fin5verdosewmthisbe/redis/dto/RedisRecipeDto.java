package com.example.be12fin5verdosewmthisbe.redis.dto;

import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RedisRecipeDto {
    private Long recipeId;
    private String storeInventoryName;
    private BigDecimal price;
    private BigDecimal quantity;

    public static RedisRecipeDto fromRecipe(Recipe recipe) {
        return new RedisRecipeDto(
                recipe.getId(),
                recipe.getStoreInventory().getName(),  // 재료 이름을 저장
                recipe.getPrice(),
                recipe.getQuantity()
        );
    }
}
