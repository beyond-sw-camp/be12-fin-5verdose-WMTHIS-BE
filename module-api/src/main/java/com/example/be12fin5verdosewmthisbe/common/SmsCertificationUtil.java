package com.example.be12fin5verdosewmthisbe.common;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmsCertificationUtil {
    @Value("${coolsms.apikey}") // coolsms의 API 키 주입
    private String apiKey;

    @Value("${coolsms.apisecret}") // coolsms의 API 비밀키 주입
    private String apiSecret;

    @Value("${coolsms.fromnumber}") // 발신자 번호 주입
    private String fromNumber;

    DefaultMessageService messageService; // 메시지 서비스를 위한 객체

    @PostConstruct // 의존성 주입이 완료된 후 초기화를 수행하는 메서드
    public void init(){
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr"); // 메시지 서비스 초기화
    }

    // 단일 메시지 발송
    public void sendSMS(String to, String certificationCode){
        try {
            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(to);
            message.setText("[WMTHIS] 본인확인 인증번호는 " + certificationCode + "  입니다.");

            this.messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            System.err.println("SMS 전송 실패: " + e.getMessage());
            throw new CustomException(ErrorCode.SMS_SEND_FAILED); // CustomException 던지기
        }
    }
}