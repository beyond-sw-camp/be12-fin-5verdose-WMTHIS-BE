package com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class MenuRegisterDto {

    @Data
    @Builder
    public static class MenuCreateRequestDto {

        @NotBlank(message = "메뉴 이름은 필수입니다.")
        @Size(max = 100, message = "메뉴 이름은 100자 이하로 입력해주세요.")
        private String name;

        @Positive(message = "카테고리 ID는 양수여야 합니다.")
        private Long categoryId;

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        private int price;

        @Valid
        private List<IngredientDto> ingredients;

        @Data
        @Builder
        public static class IngredientDto {

            @NotNull(message = "재고 ID는 필수입니다.")
            @Positive(message = "재고 ID는 양수여야 합니다.")
            private Long storeInventoryId;

            @NotNull(message = "사용량은 필수입니다.")
            @DecimalMin(value = "0.001", message = "사용량은 0.001 이상이어야 합니다.")
            private BigDecimal quantity;
        }
    }
}
