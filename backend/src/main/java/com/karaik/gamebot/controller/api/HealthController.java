package com.karaik.gamebot.controller.api;

import com.karaik.gamebot.common.api.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {

    @GetMapping
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }
}

