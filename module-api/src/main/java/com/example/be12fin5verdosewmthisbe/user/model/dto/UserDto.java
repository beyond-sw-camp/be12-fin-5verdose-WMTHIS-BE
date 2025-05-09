package com.example.be12fin5verdosewmthisbe.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class UserDto {
    // Your code here
    @Getter
    public static class LoginRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        private String email;
        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }
}
        