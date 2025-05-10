package com.example.be12fin5verdosewmthisbe.redis.dto;

import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Menu;
import com.example.be12fin5verdosewmthisbe.menu_management.menu.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RedisMenuDto {
    private Long menuId;
    private String name;
    private int price;
    private List<RedisRecipeDto> recipes;  // 레시피를 포함


    public static RedisMenuDto fromMenu(Menu menu) {
        // Menu 엔티티에서 DTO로 변환
        List<RedisRecipeDto> recipeDtos = menu.getRecipeList().stream()
                .map(Recipe::toRedisRecipeDto)  // Recipe에서 RedisRecipeDto로 변환
                .collect(Collectors.toList());

        return new RedisMenuDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                recipeDtos
        );
    }
}