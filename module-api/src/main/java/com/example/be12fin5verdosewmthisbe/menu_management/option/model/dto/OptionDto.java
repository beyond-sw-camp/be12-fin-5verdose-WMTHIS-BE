package com.example.be12fin5verdosewmthisbe.menu_management.option.model.dto;


import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.OptionValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class OptionDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryQuantityDto {

        @NotNull(message = "재고 ID는 필수입니다.")
        @Positive(message = "재고 ID는 양수여야 합니다.")
        private Long inventoryId;

        @NotNull(message = "수량은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = false, message = "수량은 0보다 커야 합니다.")
        private BigDecimal quantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequestDto {
        @NotBlank(message = "옵션 이름은 필수입니다.")
        private String name;

        @NotNull
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        private int price;

        @Valid
        private List<InventoryQuantityDto> inventoryQuantities;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseDto {
        private Long optionId;
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class IngredientDto {
        private Long storeInventoryId;
        private String name;
        private BigDecimal quantity;
        private String unit;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DetailResponseDto {
        private Long id;
        private String name;
        private int price;
        private List<IngredientDto> ingredients;

        public static DetailResponseDto from(Option option) {
            return DetailResponseDto.builder()
                    .id(option.getId())
                    .name(option.getName())
                    .price(option.getPrice())
                    .ingredients(option.getOptionValueList().stream()
                            .map(ov -> IngredientDto.builder()
                                    .storeInventoryId(ov.getStoreInventory().getId())
                                    .name(ov.getStoreInventory().getName())
                                    .quantity(ov.getQuantity())
                                    .unit(ov.getStoreInventory().getUnit())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequestDto {
        @NotNull(message = "옵션 ID는 필수입니다.")
        @Positive(message = "옵션 ID는 양수여야 합니다.")
        private Long optionId;

        @NotBlank(message = "옵션 이름은 필수입니다.")
        private String name;

        @NotNull
        @Min(value = 1, message = "가격은 1 이상이어야 합니다.")
        private int price;

        @Valid
        private List<InventoryQuantityDto> inventoryQuantities;
    }

}
