package com.example.be12fin5verdosewmthisbe.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseResponse<T> {
    private int code;       // 응답 코드 (예: 200, 400, 500)
    private String message; // 응답 메시지
    private T data;         // 응답 데이터 (제네릭)

    // 성공 응답을 위한 정적 메서드
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "Success", data);
    }

    // 실패 응답을 위한 정적 메서드
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getStatus(), errorCode.getMessage(),  null);
    }
}
