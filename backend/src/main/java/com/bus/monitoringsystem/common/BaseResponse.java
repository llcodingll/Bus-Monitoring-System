package com.bus.monitoringsystem.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseResponse<T> {

    private Boolean isSuccess;
    private int code;
    private String message;
    private T result;

    public static <T> BaseResponse<T> success(T result) {

        BaseResponse<T> response = new BaseResponse<>();
        response.isSuccess = true;
        response.code = BaseResponseStatus.SUCCESS.getCode();
        response.message = BaseResponseStatus.SUCCESS.getMessage();
        response.result = result;
        return response;
    }

    public static BaseResponse<Void> error(BaseResponseStatus status) {

        BaseResponse<Void> response = new BaseResponse<>();
        response.isSuccess = false;
        response.code = status.getCode();
        response.message = status.getMessage();
        return response;
    }

    public static BaseResponse<Void> error(BaseResponseStatus status, String message) {

        BaseResponse<Void> response = new BaseResponse<>();
        response.isSuccess = false;
        response.code = status.getCode();
        response.message = message;
        return response;
    }
}
