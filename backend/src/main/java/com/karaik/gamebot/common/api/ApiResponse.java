package com.karaik.gamebot.common.api;

/**
 * 统一响应包装，便于前后端使用一致的返回格式。
 *
 * @param code    业务状态码，0 表示成功
 * @param message 描述信息
 * @param data    具体数据
 * @param <T>     数据类型
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
