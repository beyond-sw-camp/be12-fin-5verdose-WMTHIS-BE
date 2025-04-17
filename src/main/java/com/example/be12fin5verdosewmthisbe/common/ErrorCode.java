package com.example.be12fin5verdosewmthisbe.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    ERROR_CODE(500,"예상치 못한 서버 오류;;"),
    
    // 카테고리 관련 에러코드
    INVAILD_REQUEST(5001,"카테고리 타입에 맞지 않는 잘못된 요청입니다."),
    CATEGORY_NOT_FOUND(5002,"해당 카테고리를 찾을 수 없습니다."),
    EMPTY(5003,"카테고리 목록이 비어있습니다."),
    CATEGORY_ALREADY_EXISTS(5004,"이미 존재하는 카테고리입니다."),
    
    // 옵션 관련 에러코드
    OPTION_NOT_FOUND(4001,"옵션을 찾을 수 없습니다." ),

    // 메뉴 관련 에러코드
    MENU_NOT_FOUND(3001, "메뉴를 찾을 수 없습니다." ),
    RECIPE_DUPLICATED_INVENTORY(3002, "중복된 재료를 추가할 수 없습니다."),
    RECIPE_QUANTITY_INVALID(3003, "재료의 수량은 0보다 같거나 작을 수 없습니다." ),

    // 결제 관련 에러코드
    PAYMENT_VERIFICATION_FAILED(2001,"올바른 결제가 아닙니다."),
    PAYMENT_CANCEL_FAILED(2002,"결제 취소가 실패했습니다."),
    PAYMENT_EMPTY_BODY(2003,"결제 api의 Body가 비어있습니다."),
    PAYMENT_AUTH_FAILED(2004,"Access Token 발급이 실패했습니다."),


    // 유저 관련 에러코드
    USER_NOT_FOUND(1001, "사용자가 존재하지 않습니다."),
    INVALID_PASSWORD(1002,"비밀번호가 올바르지 않습니다."),
    EMAIL_ALREADY_EXISTS(1003,"이미 이메일로 등록된 계정이 존재합니다"),
    BUSINESSNUMBER_ALREADY_EXISTS(1004,"이미 사업자 번호로 등록된 계정이 존재합니다"),
    PHONENUMBER_ALREADY_EXISTS(1005,"이미 휴대폰 번호로 등록된 계정이 존재합니다"),
    SSN_ALREADY_EXISTS(1006,"이미 주민번호로 등록된 계정이 존재합니다"),
    EMAIL_ALREADY_EXPIRED(1007, "인증 가능 시간이 만료되었습니다."),
    EMAILCODE_NOT_MATCH(1008, "이메일로 전송된 코드와 일치하지 않습니다."),
    SMS_SEND_FAILED(1009,"SMS 전송에 실패했습니다."),
    VERIFICATION_NOT_FOUND(1010, "인증 요청을 찾을 수 없습니다."),
    ALREADY_VERIFIED(1011, "이미 인증이 완료된 전화번호입니다."),
    INVALID_VERIFICATION_CODE(1012, "인증번호가 올바르지 않습니다."),
    EXPIRED_VERIFICATION_CODE(1013, "인증번호가 만료되었습니다."),
    PHONE_NOT_VERIFIED(1014, "전화번호가 인증되지 않았습니다."),

    // 장터 관련 에러코드
    SALE_NOT_FOUND(6001,"해당 판매 정보를 찾을 수 없습니다."),
    PURCHASE_NOT_FOUND(6002,"해당 구매 정보를 찾을 수 없습니다."),


    // 상점 관련 에러코드
    STORE_NOT_EXIST(8001,"상점을 찾을 수 없습니다."),

    // 재고 관련 에러코드
    INVENTORY_NOT_FOUND(7001,"해당 재고 정보를 찾을 수 없습니다."),
    INVENTORY_DELETE_FAIL(7002,"해당 재고 정보를 삭제를 실패했습니다."),
    INVENTORY_UPDATE_FAIL(7003,"해당 재고 수정에 실패했습니다."),
    INVENTORY_REGISTER_FAIL(7004,"해당 재고 정보를 등록하는데 실패했습니다."),
    INVENTORY_DUPLICATE_NAME(7005, "이미 존재하는 재고 이름입니다.");


    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
