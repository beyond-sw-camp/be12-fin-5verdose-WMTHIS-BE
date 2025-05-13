package com.example.be12fin5verdosewmthisbe.redis.dto;

import com.example.be12fin5verdosewmthisbe.inventory.repository.StoreInventoryRepository;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisMenuDto {
    private Long menuId;
    private String name;
    private int price;
    private RedisCategoryDto category; // 순환 참조 방지된 Category DTO
    private List<RedisRecipeDto> recipes;

    public static RedisMenuDto fromMenu(Menu menu) {
        return RedisMenuDto.builder()
                .menuId(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .category(RedisCategoryDto.fromCategory(menu.getCategory())) // Category DTO 사용
                .recipes(menu.getRecipeList().stream()
                        .map(Recipe::toRedisRecipeDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public Menu toMenu(StoreInventoryRepository storeInventoryRepository) {
        Menu menu = new Menu();
        menu.setId(this.menuId);
        menu.setName(this.name);
        menu.setPrice(this.price);
        menu.setCategory(this.category.toCategory());
        menu.setRecipeList(this.recipes.stream()
                .map(redisRecipeDto -> redisRecipeDto.toRecipe(storeInventoryRepository))
                .collect(Collectors.toList()));

        return menu;
    }
}