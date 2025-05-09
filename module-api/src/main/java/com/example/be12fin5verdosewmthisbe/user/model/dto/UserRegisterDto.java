package com.example.be12fin5verdosewmthisbe.user.model.dto;

import com.example.be12fin5verdosewmthisbe.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

public class UserRegisterDto {

    @Getter
    public static class SignupRequest {

        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "이름은 1~100자 사이여야 합니다.")
        private String name;

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+{};:,<.>]).{8,}$",
                message = "비밀번호는 8자리 이상이며, 대문자와 특수문자를 포함해야 합니다."
        )
        private String password;

        @NotBlank(message = "사업자 등록번호는 필수입니다.")
        @Pattern(
                regexp = "^\\d{3}-\\d{2}-\\d{5}$",
                message = "사업자 등록번호는 000-00-00000 형식이어야 합니다."
        )
        private String businessNumber;

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^(01[0-9])([0-9]{8})$", message = "전화번호는 010xxxxxxxx 형식이어야 합니다.")
        private String phoneNumber;

        @NotBlank(message = "주민등록번호는 필수입니다.")
        @Pattern(regexp = "^\\d{6}-\\d{1}$", message = "주민등록번호는 yymmdd-1 형식이어야 합니다.")
        private String ssn;

        public User toEntity(String encodedPassword) {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(encodedPassword)
                    .businessNumber(businessNumber)
                    .phoneNumber(phoneNumber)
                    .ssn(ssn)
                    .build();
            return user;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class SignupResponse {
        private Long id;
        private String name;
        private String message;

        public static SignupResponse from(User user) {
            return SignupResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .message("회원가입 성공")
                    .build();
        }
    }

}
