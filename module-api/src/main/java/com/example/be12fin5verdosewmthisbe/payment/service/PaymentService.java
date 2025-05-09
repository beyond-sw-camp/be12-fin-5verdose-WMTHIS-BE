package com.example.be12fin5verdosewmthisbe.payment.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.payment.model.Payment;
import com.example.be12fin5verdosewmthisbe.payment.model.Payment.*;
import com.example.be12fin5verdosewmthisbe.payment.model.dto.PaymentDto;
import com.example.be12fin5verdosewmthisbe.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLOutput;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;


    @Value("${portone.api.key}")
    private String apiKey;

    @Value("${portone.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Content-Type 변경

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>(); // MultiValueMap 사용
        body.add("imp_key", apiKey);
        body.add("imp_secret", apiSecret);
        System.out.println(body);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.iamport.kr/users/getToken", request, Map.class
        );
        System.out.println(response.getBody());
        // ✅ 응답 바디 확인
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("response") == null) {
            throw new CustomException(ErrorCode.PAYMENT_AUTH_FAILED);
        }

        Map<String, Object> res = (Map<String, Object>) responseBody.get("response");
        String token = (String) res.get("access_token");

        if (token == null) {
            throw new CustomException(ErrorCode.PAYMENT_AUTH_FAILED);
        }
        return token;
    }


    public PaymentDto.PaymentData savePayment(String impUid) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.iamport.kr/payments/" + impUid,
                HttpMethod.GET,
                entity,
                Map.class
        );
        Map<String, Object> body = response.getBody();
        if (body == null || body.get("response") == null) {
            throw new CustomException(ErrorCode.PAYMENT_EMPTY_BODY);
        }
        Map<String, Object> data = (Map<String, Object>) body.get("response");

        Payment payment = new Payment();
        payment.setAmount(((Number) data.get("amount")).intValue());
        payment.setTransactionId((String) data.get("imp_uid"));

        // 결제 수단 매핑
        String payMethod = (String) data.get("pay_method");
        switch (payMethod) {
            case "card" -> payment.setMethod(PaymentMethod.CREDIT_CARD);
            case "kakaopay" -> payment.setMethod(PaymentMethod.KAKAOPAY);
            default -> payment.setMethod(PaymentMethod.BANK_TRANSFER);
        }

        // 상태
        String status = (String) data.get("status");
        payment.setStatus("paid".equals(status) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);

        // 결제일
        Number paidAt = (Number) data.get("paid_at");
        if (paidAt != null) {
            payment.setPaymentDate(Timestamp.from(Instant.ofEpochSecond(paidAt.longValue())));
        }
        paymentRepository.save(payment);
        return new PaymentDto.PaymentData(
                (String) data.get("merchant_uid"),
                ((Number) data.get("amount")).intValue(),
                payment.getId()
        );
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public void cancelPayment(String impUid, String reason, Integer amount) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("imp_uid", impUid);
        body.add("reason", reason);
        body.add("amount", String.valueOf(amount));          // 환불 금액 (전체 환불이면 생략 가능)


        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.iamport.kr/payments/cancel",
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.get("code").equals(0)) {
            // 환불 성공
        } else {
            // 실패 시 로그 확인
            System.out.println("결제 취소 실패: " + responseBody.get("message"));
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

}
        