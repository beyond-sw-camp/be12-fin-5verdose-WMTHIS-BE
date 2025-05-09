package com.example.be12fin5verdosewmthisbe.payment.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.inventory.model.Inventory;
import com.example.be12fin5verdosewmthisbe.market_management.market.model.InventoryPurchase;
import com.example.be12fin5verdosewmthisbe.market_management.market.service.MarketService;
import com.example.be12fin5verdosewmthisbe.payment.model.Payment;
import com.example.be12fin5verdosewmthisbe.payment.model.dto.PaymentCancelDto;
import com.example.be12fin5verdosewmthisbe.payment.model.dto.PaymentDto;
import com.example.be12fin5verdosewmthisbe.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 관련 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final MarketService marketService;
    private final PaymentService paymentService;

    @Operation(summary = "결제 검증", description = "결제 검증 및 DB 저장")
    @ApiResponse(responseCode = "200", description = "결제 성공", content = @Content(schema = @Schema(implementation = BaseResponse.class, example = "{\"success\": true, \"data\": \"ok\", \"error\": null}")))
    @ApiResponse(responseCode = "2001", description = "결제 검증 실패", content = @Content(schema = @Schema(implementation = BaseResponse.class, example = "{\"success\": false, \"data\": null, \"error\": {\"code\": \"PAYMENT_VERIFICATION_FAILED\", \"message\": \"올바른 결제가 아닙니다.\"}}")))
    @ApiResponse(responseCode = "2003", description = "api 오류", content = @Content(schema = @Schema(implementation = BaseResponse.class, example = "{\"success\": false, \"data\": null, \"error\": {\"code\": \"PAYMENT_VERIFICATION_FAILED\", \"message\": \"결제 api의 Body가 비어있습니다.\"}}")))
    @ApiResponse(responseCode = "2004", description = "Access Token 발급 실패", content = @Content(schema = @Schema(implementation = BaseResponse.class, example = "{\"success\": false, \"data\": null, \"error\": {\"code\": \"PAYMENT_VERIFICATION_FAILED\", \"message\": \"Access Token 발급이 실패했습니다.\"}}")))
    @PostMapping("/verify")
    public BaseResponse<String> verifyPayment(@Parameter(description = "결제 검증 요청 정보", required = true) @RequestBody @Valid PaymentDto.PaymentVerifyRequest request) {


        PaymentDto.PaymentData paymentData = paymentService.savePayment(request.getImpUid());
        InventoryPurchase inventoryPurchase = marketService.findPurchaseById(request.getInventoryPurchaseId());

        int amount = paymentData.getAmount();
        int DBamount = inventoryPurchase.getPrice();
        // 금액 등 검증
        if (!request.getMerchantUid().equals(paymentData.getMerchantUid()) || amount != DBamount) {
            // 주문정보테이블 수정 - status = cancelled
            Payment payment = paymentService.findById(paymentData.getPaymentId());
            payment.setStatus(Payment.PaymentStatus.FAILED);
            // 전액 결제 취소
            paymentService.cancelPayment(payment.getTransactionId(),"결제 정보 불일치",payment.getAmount());
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }
        marketService.statusChange(request.getInventoryPurchaseId());

        // 주문정보테이블 수정 - status = paid
        return BaseResponse.success("ok");
    }

    @Operation(summary = "결제 취소", description = "결제 취소 요청")
    @ApiResponse(responseCode = "200", description = "결제 취소 성공", content = @Content(schema = @Schema(implementation = BaseResponse.class, example = "{\"success\": true, \"data\": \"ok\", \"error\": null}")))
    @ApiResponse(responseCode = "2002", description = "결제 취소 실패", content = @Content(schema = @Schema(implementation = BaseResponse.class, example = "{\"success\": false, \"data\": null, \"error\": {\"code\": \"PAYMENT_VERIFICATION_FAILED\", \"message\": \"결제 취소가 실패했습니다.\"}}")))
    @PostMapping("/cancel")
    public BaseResponse<String> cancelPayment(@Parameter(description = "결제 취소 요청 정보", required = true) @RequestBody @Valid PaymentCancelDto.RequestDto request) {

        paymentService.cancelPayment(request.getImpUid(), request.getReason(),request.getAmount());

        return BaseResponse.success("ok");
    }
}