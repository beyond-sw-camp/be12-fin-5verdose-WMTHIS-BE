package com.example.be12fin5verdosewmthisbe.payment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

public class PaymentCancelDto {
    @Data
    @AllArgsConstructor
    @Schema(description = "결제 취소 요청 DTO")
    public static class RequestDto {
        @NotBlank(message = "impUid는 필수입니다.")
        @Schema(description = "고유 거래 ID", example = "imp_abcdefg")
        private String impUid;

        @NotBlank(message = "취소 요청 금액은 필수입니다.")
        @Schema(description = "취소 요청 금액", example = "5000")
        private Integer amount;

        @NotBlank(message = "취소 사유는 필수입니다.")
        @Schema(description = "취소 사유", example = "고객 변심")
        private String reason;
    }

    @Data
    @Schema(description = "결제 취소 응답 DTO")
    public static class ResponseDto {
        @Schema(description = "고유 거래 ID", example = "imp_abcdefg")
        private String impUid;
        @Schema(description = "가맹점 주문 번호", example = "order-789")
        private String merchantUid;
        @Schema(description = "주문 ID (DB)", example = "789")
        private Long orderId;
        @Schema(description = "재고 구매 ID (DB)", example = "101")
        private Long inventoryPurchaseId;
    }
}