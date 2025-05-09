package com.example.be12fin5verdosewmthisbe.email.controller;

import com.example.be12fin5verdosewmthisbe.common.BaseResponse;
import com.example.be12fin5verdosewmthisbe.common.ErrorCode;
import com.example.be12fin5verdosewmthisbe.email.model.dto.EmailDto;
import com.example.be12fin5verdosewmthisbe.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/sendcode")
    @Operation(summary = "이메일 요청", description = "회원가입을 위해 이메일 인증요청을 하는 API 입니다")
    public BaseResponse<String> sendCode(@RequestBody EmailDto.EmailRequest dto) {
        emailService.AuthRequest(dto);
        return BaseResponse.success("성공");
    }

    @PostMapping("/sendcodeifpwfind")
    @Operation(summary = "비밀번호 찾기 이메일 요청", description = "비밀번호 찾기를 위한 이메일 인증요청을 하는 API 입니다")
    public BaseResponse<String> sendCodeifpwfind (@RequestBody EmailDto.EmailRequest dto) {
        emailService.sendCodeifpwfind(dto);
        return BaseResponse.success("성공");
    }

    @PostMapping("/authcode")
    @Operation(summary = "이메일 인증번호 확인", description = "이메일로 받은 인증 코드를 확인하는 API 입니다")
    public BaseResponse<String> authCode(@RequestBody EmailDto.EmailAuthRequest dto) {
        emailService.verifyCode(dto);
        return BaseResponse.success("성공");
    }

}
        