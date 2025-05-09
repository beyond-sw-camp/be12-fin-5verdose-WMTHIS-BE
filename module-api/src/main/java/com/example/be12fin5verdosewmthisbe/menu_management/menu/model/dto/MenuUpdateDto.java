package com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import java.util.List;

public class MenuUpdateDto {

    @Data
    @Builder
    public static class RequestDto {
        @NotNull(message = "메뉴 ID는 필수입니다.")
        @Positive(message = "메뉴 ID는 양수여야 합니다.")
        private Long menuId;

        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자 이하로 입력해주세요.")
        private String name;

        @Positive(message = "카테고리 ID는 양수여야 합니다.")
        private Long categoryId;

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        private int price;

        @Valid
        private List<MenuRegisterDto.MenuCreateRequestDto.IngredientDto> ingredients;

    }
}