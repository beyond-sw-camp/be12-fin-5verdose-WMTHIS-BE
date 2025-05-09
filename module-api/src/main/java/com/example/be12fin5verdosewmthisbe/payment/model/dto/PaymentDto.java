package com.example.be12fin5verdosewmthisbe.payment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

public class PaymentDto {
    @Data
    @AllArgsConstructor
    @Schema(description = "결제 요청 및 응답 DTO")
    public static class PaymentData {
        @NotBlank(message = "merchantUid는 필수입니다.")
        @Schema(description = "가맹점 주문 번호", example = "order-123")
        private String merchantUid;

        @NotBlank(message = "결제 금액은 필수입니다.")
        @Schema(description = "결제 금액", example = "10000")
        private int amount;

        @NotBlank(message = "결제 ID는 필수입니다.")
        @Schema(description = "결제 ID (DB)", example = "1")
        private Long paymentId;
    }

    @Data
    @Schema(description = "결제 검증 요청 DTO")
    public static class PaymentVerifyRequest {

        @NotBlank(message = "impUid는 필수입니다.")
        @Schema(description = "고유 거래 ID", example = "imp_1234567890")
        private String impUid;

        @NotBlank(message = "merchantUid는 필수입니다.")
        @Schema(description = "가맹점 주문 번호", example = "order-123")
        private String merchantUid;

        @NotBlank(message = "inventoryPurchaseId는 필수입니다.")
        @Schema(description = "재고 구매 ID (DB)", example = "456")
        private Long inventoryPurchaseId;
    }

}