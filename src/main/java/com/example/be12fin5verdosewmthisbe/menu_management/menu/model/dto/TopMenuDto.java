package com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopMenuDto {
    private Long storeId;
    private Long menuId;
    private Long totalCount;
}
