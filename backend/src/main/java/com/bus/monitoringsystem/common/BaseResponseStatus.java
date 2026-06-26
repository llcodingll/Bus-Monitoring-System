package com.bus.monitoringsystem.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BaseResponseStatus {

    SUCCESS(true, 20000, "요청에 성공하였습니다."),
    INVALID_INPUT(false, 40000, "잘못된 입력입니다."),
    INTERNAL_SERVER_ERROR(false, 50000, "서버 오류가 발생했습니다."),

    BUS_NOT_FOUND(false, 40401, "존재하지 않는 버스입니다."),
    EVENT_NOT_FOUND(false, 40402, "존재하지 않는 이벤트입니다."),
    SEED_FAILED(false, 50001, "목업 데이터 생성에 실패했습니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;
}
