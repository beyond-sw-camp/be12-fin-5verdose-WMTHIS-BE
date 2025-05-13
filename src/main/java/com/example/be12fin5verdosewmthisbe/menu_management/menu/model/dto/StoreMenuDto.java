package com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "가게별 메뉴 및 옵션 정보 DTO")
public class StoreMenuDto {
    @Schema(description = "메뉴 ID", example = "9")
    private Long menuId;

    @Schema(description = "메뉴명", example = "불고기버거")
    private String menuName;

    @Schema(description = "기본 가격", example = "13408")
    private Integer price;

    @Schema(description = "사용 가능한 옵션 ID 목록", example = "[1,2,3]")
    private List<Long> optionIds;
}