package com.example.be12fin5verdosewmthisbe.store.model.dto;


import com.example.be12fin5verdosewmthisbe.market_management.market.model.dto.InventorySaleDto;
import com.example.be12fin5verdosewmthisbe.store.model.Store;
import com.example.be12fin5verdosewmthisbe.user.model.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;


public class StoreDto {
    // Your code here

    @Getter
    public static class RegistRequest{
        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "이름은 1~100자 사이여야 합니다.")
        private String name;
        @NotBlank(message = "주소는 필수입니다.")
        @Size(min = 5, max = 255, message = "주소는 5~255자 사이여야 합니다.")
        private String address;
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^(0[2-9]{1}[0-9]{1})-([0-9]{3,4})-([0-9]{4})$", message = "유효한 전화번호 형식이 아닙니다.")
        private String phoneNumber;
        @DecimalMin(value = "-90", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90", message = "위도는 90 이하이어야 합니다.")
        private Double latitude;
        @DecimalMin(value = "-180", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180", message = "경도는 180 이하이어야 합니다.")
        private Double longitude;

        public Store toEntity(User user,Double latitude,Double longitude){
            return Store.builder()
                    .name(name)
                    .address(address)
                    .phoneNumber(phoneNumber)
                    .latitude(latitude)
                    .longitude(longitude)
                    .user(user)
                    .build();
        }

    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class response {

        private String name;
        private String address;
        private String phoneNumber;
        private Double latitude;
        private Double longitude;
        private List<InventorySaleDto.InventorySaleListDto> boardList;
    }
}
        