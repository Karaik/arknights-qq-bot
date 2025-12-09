package com.karaik.gamebot.common.config;

import com.karaik.gamebot.auth.config.AuthProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/Knife4j 配置，默认携带 X-API-KEY 便于调试。
 */
@Configuration
public class OpenApiConfig {

    private static final String API_KEY_SCHEME = "api_key";

    @Bean
    public OpenAPI openAPI(AuthProperties authProperties) {
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("X-API-KEY")
                .in(SecurityScheme.In.HEADER)
                .description("全局 API Key，默认值用于本地调试");

        SecurityRequirement requirement = new SecurityRequirement().addList(API_KEY_SCHEME);

        return new OpenAPI()
                .info(new Info().title("Arknights Auth API").version("v1"))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, apiKey))
                .addSecurityItem(requirement);
    }
}
