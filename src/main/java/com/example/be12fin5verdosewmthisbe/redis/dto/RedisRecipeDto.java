package com.example.be12fin5verdosewmthisbe.redis.dto;

import com.example.be12fin5verdosewmthisbe.inventory.model.StoreInventory;
import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisRecipeDto {
    private Long recipeId;
    private Long storeInventoryId;  // storeInventoryId로 변경
    private BigDecimal quantity;

    // Recipe 객체를 RedisRecipeDto로 변환하는 메서드
    public static RedisRecipeDto fromRecipe(Recipe recipe) {
        return new RedisRecipeDto(
                recipe.getId(),
                recipe.getStoreInventory().getId(),
                recipe.getQuantity()
        );
    }

    // RedisRecipeDto를 Recipe 객체로 변환하는 메서드
    public Recipe toRecipe(StoreInventoryRepository storeInventoryRepository) {
        StoreInventory storeInventory = storeInventoryRepository.findById(this.storeInventoryId)
                .orElseThrow(() -> new RuntimeException("StoreInventory not found"));

        return new Recipe(this.recipeId, storeInventory, this.quantity);
    }
}