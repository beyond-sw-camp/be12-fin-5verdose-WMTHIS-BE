package com.example.be12fin5verdosewmthisbe.user.model.dto;

import com.example.be12fin5verdosewmthisbe.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserInfoDto {

    @Getter
    public static class UpdateRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+{};:,<.>]).{8,}$",
                message = "비밀번호는 8자리 이상이며, 대문자와 특수문자를 포함해야 합니다."
        )
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+{};:,<.>]).{8,}$",
                message = "비밀번호는 8자리 이상이며, 대문자와 특수문자를 포함해야 합니다."
        )
        private String newPassword;

        public User toEntity(String encodedPassword) {
            return User.builder()
                    .email(email)
                    .password(encodedPassword)
                    .build();
        }
    }

    @Getter
    public static class PasswordRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+{};:,<.>]).{8,}$",
                message = "비밀번호는 8자리 이상이며, 대문자와 특수문자를 포함해야 합니다."
        )
        private String password;
    }

    @Getter
    public static class NewPasswordRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+{};:,<.>]).{8,}$",
                message = "비밀번호는 8자리 이상이며, 대문자와 특수문자를 포함해야 합니다."
        )
        private String newPassword;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class SearchResponse {
        private String phoneNumber;
        private String name;
        private String email;
        private String ssn;

        public static SearchResponse from(User user) {
            String newSsn = user.getSsn();
            if (newSsn != null && newSsn.length() > 8) {
                newSsn = newSsn.substring(0, 8) + "*".repeat(6);
            }

            return SearchResponse.builder()
                    .phoneNumber(user.getPhoneNumber())
                    .name(user.getName())
                    .email(user.getEmail())
                    .ssn(newSsn)
                    .build();
        }
    }
}
