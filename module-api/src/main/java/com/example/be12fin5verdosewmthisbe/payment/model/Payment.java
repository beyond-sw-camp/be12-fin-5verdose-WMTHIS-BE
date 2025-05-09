package com.example.be12fin5verdosewmthisbe.payment.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "payment")
@Data
@Schema(description = "결제 정보")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "결제 ID", example = "1")
    private Long id;

    @Column(name = "amount")
    @Schema(description = "결제 금액", example = "10000")
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    @Schema(description = "결제 수단", example = "CREDIT_CARD", allowableValues = {"CREDIT_CARD", "KAKAOPAY"})
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "결제 상태", example = "PENDING", allowableValues = {"SUCCESS", "FAILED"})
    private PaymentStatus status;

    @Column(name = "transaction_id", length = 100)
    @Schema(description = "결제사에서 발급하는 거래 고유 ID", example = "tx-1234567890")
    private String transactionId;

    @Column(name = "payment_date")
    @Schema(description = "결제 시간", example = "2023-10-27T10:00:00Z")
    private Timestamp paymentDate;

    public enum PaymentMethod {
        CREDIT_CARD,
        BANK_TRANSFER,
        KAKAOPAY
    }

    public enum PaymentStatus {
        SUCCESS,
        FAILED
    }
}