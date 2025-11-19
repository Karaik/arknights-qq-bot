package com.karaik.gamebot.roguelike.controller.api;

import com.karaik.gamebot.common.api.ApiResponse;
import com.karaik.gamebot.roguelike.client.RoguelikeApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一将肉鸽相关异常转换为可读的响应，避免 500。
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.karaik.gamebot")
public class RoguelikeExceptionHandler {

    @ExceptionHandler(RoguelikeApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoguelikeApiException(RoguelikeApiException ex) {
        log.warn("event=roguelike.api.error message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }
}
