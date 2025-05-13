package com.example.be12fin5verdosewmthisbe.redis.dto;

import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RedisCategoryDto {
    private Long id;  // 카테고리 ID
    private String name;  // 카테고리 이름
    private Long storeId;  // 관련된 store의 ID (순환 참조 방지)

    public static RedisCategoryDto fromCategory(Category category) {
        return new RedisCategoryDto(
                category.getId(),
                category.getName(),
                category.getStore().getId()  // store의 ID만 포함시킴 (순환 참조 방지)
        );
    }

    public Category toCategory() {
        Category category = new Category();
        category.setId(this.id);
        category.setName(this.name);
        return category;
    }
}
