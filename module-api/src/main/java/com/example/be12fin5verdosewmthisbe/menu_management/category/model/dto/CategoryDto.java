package com.example.be12fin5verdosewmthisbe.menu_management.category.model.dto;

import com.example.be12fin5verdosewmthisbe.menu_management.category.model.Category;
import com.example.be12fin5verdosewmthisbe.menu_management.option.model.Option;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryDto {

    @Schema(description = "카테고리 등록 요청 DTO")
    @Getter
    public static class requestDto {

        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Schema(description = "카테고리 이름", example = "한식")
        private String name;

        @NotNull(message = "optionIds는 필수입니다.")
        private List<Long> optionIds;
    }
    @Schema(description = "카테고리 삭제 요청 DTO")
    @Getter
    public static class deleteDto {

        @NotNull(message = "ids는 필수입니다.")
        @Schema(description = "카테고리 아이디", example = "한식")
        private List<Long> ids;
    }



    @Schema(description = "카테고리 수정 요청 DTO")
    @Getter
    @Setter
    public static class updateDto {
        @NotNull(message = "카테고리 ID는 필수입니다.")
        @Positive(message = "카테고리 ID는 양수여야 합니다.")
        private Long id;

        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 100, message = "카테고리 이름은 100자 이하로 입력해주세요.")
        private String newName;

        @NotNull(message = "optionIds는 필수입니다.")
        private List<Long> optionIds;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class responseDto {
        private Long id;
        private String name;
        private List<OptionDto> options;

        public static responseDto from(Category category) {
            return responseDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .options(category.getCategoryOptions().stream()
                            .map(co -> OptionDto.from(co.getOption()))
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryResponseDto {
        private Long id;
        private String name;

        public static CategoryResponseDto fromEntity(Category category) {
            return CategoryResponseDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();
        }
    }
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class OptionDto {
        private Long id;
        private String name;
        private int price;

        public static OptionDto from(Option option) {
            return OptionDto.builder()
                    .id(option.getId())
                    .name(option.getName())
                    .price(option.getPrice())
                    .build();
        }
    }

}