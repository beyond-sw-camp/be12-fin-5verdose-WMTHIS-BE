package com.example.be12fin5verdosewmthisbe.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

public class PhoneVerificationDto {

    @Getter
    public static class SmsSendRequestDto{
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^(01[0-9])([0-9]{8})$", message = "전화번호는 010xxxxxxxx 형식이어야 합니다.")
        private String phoneNum;
    }
    @Getter
    public static class VerifyRequestDto{
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^(01[0-9])([0-9]{8})$", message = "전화번호는 010xxxxxxxx 형식이어야 합니다.")
        private String phoneNum;

        @NotBlank(message = "인증번호는 필수입니다.")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "인증번호는 6자리 숫자여야 합니다."
        )
        private String code;
    }
}
