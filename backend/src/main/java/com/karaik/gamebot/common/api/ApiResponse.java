package com.karaik.gamebot.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 提供统一格式的 API 响应封装。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
