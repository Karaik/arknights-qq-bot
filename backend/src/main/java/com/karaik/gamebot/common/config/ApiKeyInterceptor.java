package com.karaik.gamebot.common.config;

import com.karaik.gamebot.auth.config.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 简单的 Header API Key 校验，默认值可在配置 auth.hypergryph.api-key 中调整。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String HEADER_KEY = "X-API-KEY";

    private final AuthProperties authProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String required = authProperties.getHypergryph().getApiKey();
        String provided = request.getHeader(HEADER_KEY);
        if (required == null || required.isEmpty()) {
            return true;
        }
        if (required.equals(provided)) {
            return true;
        }
        log.warn("event=api_key.invalid path={} provided={}", request.getRequestURI(), provided);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":401,\"message\":\"invalid api key\"}");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // no-op
    }
}
