package com.example.be12fin5verdosewmthisbe.menu_management.menu.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class MenuInfoDto {
    @Builder
    @Getter
    public static class MenuResponse{
        private String menuName;
        private String category;

        public static MenuResponse of(String menuName, String category) {
            return MenuResponse.builder()
                    .menuName(menuName)
                    .category(category)
                    .build();
        }
    }
}
