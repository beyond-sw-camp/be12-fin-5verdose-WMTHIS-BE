package com.example.be12fin5verdosewmthisbe.user.service;

import com.example.be12fin5verdosewmthisbe.common.CustomException;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.common.SmsCertificationUtil;
import com.example.be12fin5verdosewmthisbe.user.model.PhoneVerification;
import com.example.be12fin5verdosewmthisbe.user.repository.PhoneVerificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final SmsCertificationUtil smsCertificationUtil;

    @Transactional
    public String sendCertificationCode(String phoneNumber) {
        String code = generateCode(); // 예: 6자리 난수
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5);
        System.out.println(code);

        // DB 저장
        PhoneVerification verification = PhoneVerification.builder()
                .phoneNumber(phoneNumber)
                .certificationCode(code)
                .expirationTime(expiration)
                .isVerified(false)
                .build();
        phoneVerificationRepository.save(verification);
        return code;
        // SMS 전송
        //smsCertificationUtil.sendSMS(phoneNumber, code);
    }

    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    public void verifyCertificationCode(String phoneNumber, String inputCode) {
        PhoneVerification verification = phoneVerificationRepository.findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verification.isVerified()) {
            throw new CustomException(ErrorCode.ALREADY_VERIFIED);
        }

        if (!verification.getCertificationCode().equals(inputCode)) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        if (verification.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        // 성공 → 인증 상태 업데이트
        verification.setVerified(true);
        phoneVerificationRepository.save(verification);
    }

}